package io.github.mangomaner.mangobot.agent.capability.tool;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.agentscope.core.tool.coding.ShellCommandTool;
import io.agentscope.core.tool.file.ReadFileTool;
import io.agentscope.core.tool.file.WriteFileTool;
import io.github.mangomaner.mangobot.agent.tools.CalculatorTool;
import io.github.mangomaner.mangobot.agent.tools.DateTimeTool;
import io.github.mangomaner.mangobot.agent.tools.TextTool;
import io.github.mangomaner.mangobot.annotation.MangoTool;
import io.github.mangomaner.mangobot.agent.model.domain.AgentJavaToolConfig;
import io.github.mangomaner.mangobot.agent.service.AgentJavaToolConfigService;
import io.github.mangomaner.mangobot.api.MangoToolApi;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Set;
import java.util.function.Supplier;

/**
 * 工具注册服务
 * 
 * <p>负责管理 Java 工具的注册、更新和注销，提供四种注册方式：
 * <ul>
*   <li>{@link #registerTool(Class, Object...)} - 注册工具类（无参或带参）</li>
*   <li>{@link #registerToolInstance(Object, Integer)} - 注册工具实例（全局共享）</li>
*   <li>{@link #registerToolFactory(Class, Supplier, Integer)} - 注册工具工厂（每次新建）</li>
* </ul>
 * 
 * <p>内置工具通过 {@link #initBuiltInTools()} 在启动时自动注册。
 * 
 * @see JavaToolLoader
 * @see MangoToolApi
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class ToolRegistrationService {

    /** Shell 命令白名单 */
    private static final Set<String> ALLOWED_SHELL_COMMANDS = Set.of(
        "ls", "dir", "cat", "head", "tail", "grep", "find", "pwd",
        "echo", "mkdir", "rm", "cp", "mv", "touch", "chmod"
    );

    private final AgentJavaToolConfigService toolConfigService;
    private final JavaToolLoader javaToolLoader;
    private final ObjectMapper objectMapper;

    /**
     * 初始化内置工具
     * 
     * <p>在应用启动时调用，通过 {@link MangoToolApi} 注册所有内置工具：
     * <ul>
     *   <li>DateTimeTool - 日期时间工具</li>
     *   <li>CalculatorTool - 计算器工具</li>
     *   <li>TextTool - 文本处理工具</li>
     *   <li>ReadFileTool - 文件读取工具（AgentScope 内置）</li>
     *   <li>WriteFileTool - 文件写入工具（AgentScope 内置）</li>
     *   <li>ShellCommandTool - Shell 命令工具（AgentScope 内置，工厂模式）</li>
     * </ul>
     */
    public void initBuiltInTools() {
        MangoToolApi.registerTool(DateTimeTool.class);
        MangoToolApi.registerTool(CalculatorTool.class);
        MangoToolApi.registerTool(TextTool.class);
        MangoToolApi.registerTool(ReadFileTool.class);
        MangoToolApi.registerTool(WriteFileTool.class);
        MangoToolApi.registerToolFactory(ShellCommandTool.class, 
            () -> new ShellCommandTool(ALLOWED_SHELL_COMMANDS, cmd -> true));
        
        log.info("Built-in tools registered via MangoToolApi");
    }

    /**
     * 注册工具类
     * 
     * <p>支持无参数和带参数两种加载方式：
     * <ul>
     *   <li>无参数：args 为空数组，使用 {@link JavaToolLoader#LOAD_TYPE_NO_ARGS}</li>
     *   <li>带参数:args 不为空，使用 {@link JavaToolLoader#LOAD_TYPE_WITH_ARGS}，li>
     * </ul>
     * 
     * <p>参数会自动序列化为 JSON 存储到数据库。
     * 支持基本类型: String、集合、Map 等可 JSON 序列化的类型。
     * 
     * @param toolClass 工具类
     * @param args 构造参数（可变参数，自动序列化）
     * @return 工具配置 ID
     * @throws IllegalArgumentException 如果参数无法序列化
     */
    public Integer registerTool(Class<?> toolClass, Object... args) {
        return registerToolInternal(toolClass, args, null);
    }

    /**
     * 注册工具类（来自插件）
     * 
     * <p>对于插件工具，会在注册时立即创建实例并存入缓存，
     * 解决插件 ClassLoader 隔离导致的类加载问题。
     * 
     * @param toolClass 工具类
     * @param args 构造参数数组（可为 null）
     * @param pluginId 插件 ID
     * @return 工具配置 ID
     */
    public Integer registerToolWithPluginId(Class<?> toolClass, Object[] args, Integer pluginId) {
        return registerToolInternal(toolClass, args, pluginId);
    }
    
    /**
     * 内部注册方法
     */
    private Integer registerToolInternal(Class<?> toolClass, Object[] args, Integer pluginId) {
        String className = toolClass.getName();
        ToolMetadata metadata = extractMetadata(toolClass);
        
        boolean hasArgs = args != null && args.length > 0;
        String loadType = hasArgs 
            ? JavaToolLoader.LOAD_TYPE_WITH_ARGS 
            : JavaToolLoader.LOAD_TYPE_NO_ARGS;
        
        String argsJson = hasArgs ? serializeArgs(args) : null;
        
        try {
            Object instance = createInstance(toolClass, args);
            javaToolLoader.registerInstance(className, instance);
            log.debug("Tool instance created and cached: {}", className);
        } catch (Exception e) {
            log.warn("Failed to create tool instance, will try lazy loading: {}", className, e);
        }
        
        AgentJavaToolConfig existing = toolConfigService.getByClassName(className);
        
        if (existing != null) {
            existing.setToolName(metadata.name);
            existing.setDescription(metadata.description);
            existing.setCategory(metadata.category);
            if (hasArgs) {
                existing.setConstructorArgs(argsJson);
            }
            existing.setLoadType(loadType);
            existing.setPluginId(pluginId);
            toolConfigService.updateById(existing);
            log.info("Tool updated: {} (enabled={})", className, existing.getEnabled());
            return existing.getId();
        } else {
            AgentJavaToolConfig config = new AgentJavaToolConfig();
            config.setClassName(className);
            config.setConstructorArgs(argsJson);
            config.setToolName(metadata.name);
            config.setDescription(metadata.description);
            config.setCategory(metadata.category);
            config.setLoadType(loadType);
            config.setPluginId(pluginId);
            config.setEnabled(false);
            toolConfigService.save(config);
            log.info("Tool registered: {} (disabled by default)", className);
            return config.getId();
        }
    }
    
    /**
     * 创建工具实例
     * 
     * <p>支持包装类型到原始类型的自动转换（如 Integer -> int）
     * 
     * @param toolClass 工具类
     * @param args 构造参数
     * @return 工具实例
     * @throws Exception 创建失败
     */
    private Object createInstance(Class<?> toolClass, Object[] args) throws Exception {
        if (args == null || args.length == 0) {
            return toolClass.getDeclaredConstructor().newInstance();
        }
        
        Exception lastException = null;
        for (var constructor : toolClass.getConstructors()) {
            if (constructor.getParameterCount() == args.length) {
                try {
                    Object[] convertedArgs = convertArgs(args, constructor.getParameterTypes());
                    return constructor.newInstance(convertedArgs);
                } catch (Exception e) {
                    lastException = e;
                    log.debug("Constructor {} failed: {}", constructor, e.getMessage());
                }
            }
        }
        
        throw new NoSuchMethodException("No matching constructor found for " + toolClass.getName() + 
            ". Args: " + java.util.Arrays.toString(args) + 
            ". Last error: " + (lastException != null ? lastException.getMessage() : "none"));
    }
    
    /**
     * 转换参数类型以匹配构造函数期望的类型
     * 
     * <p>支持：
     * <ul>
     *   <li>包装类型 -> 原始类型（Integer -> int, Boolean -> boolean）</li>
     *   <li>Number 子类之间的转换（Integer -> Long, Double -> Float）</li>
     * </ul>
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

    /**
     * 注册工具实例
     * 
     * <p>直接传入工具实例，全局共享。适用于：
     * <ul>
     *   <li>需要复杂初始化的工具</li>
     *   <li>有状态但希望全局共享的工具</li>
     * </ul>
     * 
     * @param toolInstance 工具实例
     * @param pluginId 插件 ID（可选）
     * @return 工具配置 ID
     */
    public Integer registerToolInstance(Object toolInstance, Integer pluginId) {
        String className = toolInstance.getClass().getName();
        ToolMetadata metadata = extractMetadata(toolInstance.getClass());
        
        javaToolLoader.registerInstance(className, toolInstance);
        
        AgentJavaToolConfig existing = toolConfigService.getByClassName(className);
        
        if (existing != null) {
            existing.setToolName(metadata.name);
            existing.setDescription(metadata.description);
            existing.setCategory(metadata.category);
            existing.setLoadType(JavaToolLoader.LOAD_TYPE_INSTANCE);
            existing.setPluginId(pluginId);
            toolConfigService.updateById(existing);
            log.info("Tool instance updated: {} (enabled={})", className, existing.getEnabled());
            return existing.getId();
        } else {
            AgentJavaToolConfig config = new AgentJavaToolConfig();
            config.setClassName(className);
            config.setToolName(metadata.name);
            config.setDescription(metadata.description);
            config.setCategory(metadata.category);
            config.setLoadType(JavaToolLoader.LOAD_TYPE_INSTANCE);
            config.setPluginId(pluginId);
            config.setEnabled(false);
            toolConfigService.save(config);
            log.info("Tool instance registered: {} (disabled by default)", className);
            return config.getId();
        }
    }

    /**
     * 注册工具工厂
     * 
     * <p>每次创建 Agent 时调用工厂创建新实例。适用于：
     * <ul>
     *   <li>需要每次创建新实例的工具（如带状态的工具）</li>
     *   <li>需要动态参数的工具（如依赖运行时配置）</li>
     * </ul>
     * 
     * @param toolClass 工具类
     * @param factory 工厂方法
     * @param pluginId 插件 ID（可选）
     * @return 工具配置 ID
     */
    public Integer registerToolFactory(Class<?> toolClass, Supplier<Object> factory, Integer pluginId) {
        String className = toolClass.getName();
        ToolMetadata metadata = extractMetadata(toolClass);
        
        javaToolLoader.registerFactory(className, factory);
        
        AgentJavaToolConfig existing = toolConfigService.getByClassName(className);
        
        if (existing != null) {
            existing.setToolName(metadata.name);
            existing.setDescription(metadata.description);
            existing.setCategory(metadata.category);
            existing.setLoadType(JavaToolLoader.LOAD_TYPE_FACTORY);
            existing.setPluginId(pluginId);
            toolConfigService.updateById(existing);
            log.info("Tool factory updated: {} (enabled={})", className, existing.getEnabled());
            return existing.getId();
        } else {
            AgentJavaToolConfig config = new AgentJavaToolConfig();
            config.setClassName(className);
            config.setToolName(metadata.name);
            config.setDescription(metadata.description);
            config.setCategory(metadata.category);
            config.setLoadType(JavaToolLoader.LOAD_TYPE_FACTORY);
            config.setPluginId(pluginId);
            config.setEnabled(false);
            toolConfigService.save(config);
            log.info("Tool factory registered: {} (disabled by default)", className);
            return config.getId();
        }
    }

    /**
     * 注销工具
     * 
     * <p>从缓存和数据库中移除工具配置
     * 
     * @param className 类全限定名
     */
    public void unregisterTool(String className) {
        javaToolLoader.registerInstance(className, null);
        javaToolLoader.registerFactory(className, null);
        toolConfigService.deleteByClassName(className);
        log.info("Tool unregistered: {}", className);
    }

    /**
     * 从类中提取工具元数据
     * 
     * <p>优先从 @MangoTool 注解获取，无注解则使用默认值
     */
    private ToolMetadata extractMetadata(Class<?> toolClass) {
        MangoTool annotation = toolClass.getAnnotation(MangoTool.class);
        
        if (annotation != null) {
            return new ToolMetadata(
                annotation.name(),
                annotation.description(),
                annotation.category()
            );
        } else {
            String simpleName = toolClass.getSimpleName();
            String name = simpleName.replace("Tool", "");
            return new ToolMetadata(name, "AgentScope built-in tool", "SYSTEM");
        }
    }

    /** 工具元数据 */
    private record ToolMetadata(String name, String description, String category) {}

    /**
     * 序列化构造参数
     * 
     * @param args 构造参数数组
     * @return JSON 字符串
     * @throws IllegalArgumentException 如果参数无法序列化
     */
    private String serializeArgs(Object[] args) {
        if (args == null || args.length == 0) {
            return null;
        }
        try {
            return objectMapper.writeValueAsString(args);
        } catch (JsonProcessingException e) {
            throw new IllegalArgumentException("参数无法序列化: " + e.getMessage());
        }
    }
}
