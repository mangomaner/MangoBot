package io.github.mangomaner.mangobot.module.agent.capability.tool;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.mangomaner.mangobot.module.agent.model.domain.AgentJavaToolConfig;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.lang.reflect.Constructor;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Supplier;

/**
 * Java 工具加载器
 * 
 * <p>负责从配置加载 Java 工具实例，支持四种加载方式：
 * <ul>
 *   <li>{@link #LOAD_TYPE_NO_ARGS} - 无参数，通过反射实例化</li>
 *   <li>{@link #LOAD_TYPE_WITH_ARGS} - 带参数，通过反射实例化并传入构造参数</li>
 *   <li>{@link #LOAD_TYPE_INSTANCE} - 实例模式，直接使用缓存的实例（全局共享）</li>
 *   <li>{@link #LOAD_TYPE_FACTORY} - 工厂模式，每次调用工厂创建新实例</li>
 * </ul>
 * 
 * <p>加载优先级：工厂缓存 → 实例缓存 → 反射实例化
 * 
 * @see ToolRegistrationService
 * @see AgentJavaToolConfig
 */
@Component
@Slf4j
public class JavaToolLoader {

    /** 无参数加载：通过反射调用无参构造函数 */
    public static final String LOAD_TYPE_NO_ARGS = "NO_ARGS";
    
    /** 带参数加载：通过反射调用带参构造函数，参数从数据库读取 */
    public static final String LOAD_TYPE_WITH_ARGS = "WITH_ARGS";
    
    /** 实例加载：使用缓存的工具实例，全局共享 */
    public static final String LOAD_TYPE_INSTANCE = "INSTANCE";
    
    /** 工厂加载：每次创建 Agent 时调用工厂创建新实例 */
    public static final String LOAD_TYPE_FACTORY = "FACTORY";

    private final ObjectMapper objectMapper = new ObjectMapper();
    
    /** 工具实例缓存：用于 INSTANCE 模式 */
    private final Map<String, Object> instanceCache = new ConcurrentHashMap<>();
    
    /** 工具工厂缓存：用于 FACTORY 模式 */
    private final Map<String, Supplier<Object>> factoryCache = new ConcurrentHashMap<>();

    /**
     * 注册工具实例
     * 
     * @param className 类全限定名
     * @param instance 工具实例
     */
    public void registerInstance(String className, Object instance) {
        if (instance != null) {
            instanceCache.put(className, instance);
            log.debug("Tool instance registered: {}", className);
        } else {
            instanceCache.remove(className);
            log.debug("Tool instance removed: {}", className);
        }
    }

    /**
     * 注册工具工厂
     * 
     * @param className 类全限定名
     * @param factory 工具工厂（每次调用返回新实例）
     */
    public void registerFactory(String className, Supplier<Object> factory) {
        if (factory != null) {
            factoryCache.put(className, factory);
            log.debug("Tool factory registered: {}", className);
        } else {
            factoryCache.remove(className);
            log.debug("Tool factory removed: {}", className);
        }
    }

    /**
     * 加载工具
     * 
     * <p>根据配置的 loadType 选择加载方式：
     * <ol>
     *   <li>优先检查实例缓存（解决插件 ClassLoader 隔离问题）</li>
     *   <li>FACTORY 模式：从工厂缓存获取，每次返回新实例</li>
     *   <li>INSTANCE 模式：从实例缓存获取，返回共享实例</li>
     *   <li>NO_ARGS/WITH_ARGS 模式：通过反射实例化</li>
     * </ol>
     * 
     * @param config 工具配置
     * @return 工具实例（加载失败返回 empty）
     */
    public Optional<Object> loadTool(AgentJavaToolConfig config) {
        String className = config.getClassName();
        String loadType = config.getLoadType() != null ? config.getLoadType() : LOAD_TYPE_NO_ARGS;

        // 优先级0：实例缓存（解决插件 ClassLoader 隔离问题）
        if (instanceCache.containsKey(className)) {
            Object tool = instanceCache.get(className);
            log.debug("Tool loaded from instance cache: {}", className);
            return Optional.of(tool);
        }

        // 优先级1：工厂模式
        if (LOAD_TYPE_FACTORY.equals(loadType) && factoryCache.containsKey(className)) {
            try {
                Object tool = factoryCache.get(className).get();
                log.debug("Tool loaded from factory: {}", className);
                return Optional.of(tool);
            } catch (Exception e) {
                log.error("Failed to create tool from factory: {}", className, e);
                return Optional.empty();
            }
        }

        // 优先级2：反射实例化（仅适用于主程序内置工具）
        return loadByReflection(config);
    }

    /**
     * 通过反射加载工具
     */
    private Optional<Object> loadByReflection(AgentJavaToolConfig config) {
        String className = config.getClassName();
        
        try {
            Class<?> toolClass = Class.forName(className);
            Object[] args = parseConstructorArgs(config.getConstructorArgs());
            
            if (args == null || args.length == 0) {
                Object tool = toolClass.getDeclaredConstructor().newInstance();
                log.debug("Tool loaded by reflection (no args): {}", className);
                return Optional.of(tool);
            }
            
            Constructor<?> constructor = findMatchingConstructor(toolClass, args);
            Object[] convertedArgs = convertArgs(args, constructor.getParameterTypes());
            Object tool = constructor.newInstance(convertedArgs);
            log.debug("Tool loaded by reflection (with args): {}", className);
            return Optional.of(tool);
            
        } catch (ClassNotFoundException e) {
            log.warn("Tool class not found: {}", className);
            return Optional.empty();
        } catch (Exception e) {
            log.error("Failed to load tool by reflection: {}", className, e);
            return Optional.empty();
        }
    }

    /**
     * 检查类是否可加载
     * 
     * @param className 类全限定名
     * @return 是否可加载
     */
    public boolean isClassLoadable(String className) {
        if (instanceCache.containsKey(className) || factoryCache.containsKey(className)) {
            return true;
        }
        try {
            Class.forName(className);
            return true;
        } catch (ClassNotFoundException e) {
            return false;
        }
    }

    /**
     * 解析构造参数 JSON
     */
    private Object[] parseConstructorArgs(String argsJson) {
        if (!StringUtils.hasText(argsJson)) {
            return null;
        }
        try {
            List<Object> argsList = objectMapper.readValue(argsJson, new TypeReference<List<Object>>() {});
            return argsList.toArray();
        } catch (Exception e) {
            log.warn("Failed to parse constructor args: {}", argsJson, e);
            return null;
        }
    }

    /**
     * 查找匹配的构造函数
     */
    private Constructor<?> findMatchingConstructor(Class<?> clazz, Object[] args) throws NoSuchMethodException {
        for (Constructor<?> constructor : clazz.getConstructors()) {
            if (constructor.getParameterCount() == args.length) {
                return constructor;
            }
        }
        throw new NoSuchMethodException("No matching constructor found for " + clazz.getName());
    }
    
    /**
     * 转换参数类型以匹配构造函数期望的类型
     */
    private Object[] convertArgs(Object[] args, Class<?>[] paramTypes) {
        Object[] result = new Object[args.length];
        for (int i = 0; i < args.length; i++) {
            result[i] = convertArg(args[i], paramTypes[i]);
        }
        return result;
    }
    
    private Object convertArg(Object arg, Class<?> targetType) {
        if (arg == null) return null;
        
        Class<?> argType = arg.getClass();
        
        if (targetType.isAssignableFrom(argType)) {
            return arg;
        }
        
        if (arg instanceof Number num) {
            if (targetType == int.class || targetType == Integer.class) {
                return num.intValue();
            } else if (targetType == long.class || targetType == Long.class) {
                return num.longValue();
            } else if (targetType == double.class || targetType == Double.class) {
                return num.doubleValue();
            } else if (targetType == float.class || targetType == Float.class) {
                return num.floatValue();
            } else if (targetType == short.class || targetType == Short.class) {
                return num.shortValue();
            } else if (targetType == byte.class || targetType == Byte.class) {
                return num.byteValue();
            }
        }
        
        if (arg instanceof Boolean bool) {
            if (targetType == boolean.class || targetType == Boolean.class) {
                return bool;
            }
        }
        
        if (arg instanceof String str) {
            if (targetType == String.class) {
                return str;
            }
        }
        
        throw new IllegalArgumentException("Cannot convert " + argType + " to " + targetType);
    }
}
