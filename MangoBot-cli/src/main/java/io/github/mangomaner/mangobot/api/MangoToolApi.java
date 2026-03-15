package io.github.mangomaner.mangobot.api;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.mangomaner.mangobot.agent.capability.tool.ToolRegistrationService;

import java.util.function.Supplier;

/**
 * Agent 工具 API (静态工具类)
 * 
 * <p>提供工具注册和注销能力，供插件和主程序使用。
 * 
 * <h3>注册方式</h3>
 * <ul>
*   <li>{@link #registerTool(Class)} - 注册工具类（无参数）</li>
*   <li>{@link #registerTool(Class, Object...)} - 注册工具类（带构造参数，自动序列化）</li>
*   <li>{@link #registerToolInstance(Object)} - 注册工具实例（全局共享)</li>
*   <li>{@link #registerToolFactory(Class, Supplier)} - 注册工具工厂（每次新建）</li>
* </ul>
 * 
 * @see ToolRegistrationService
 */
public class MangoToolApi {

    private static ToolRegistrationService toolRegistrationService;
    private static final ObjectMapper objectMapper = new ObjectMapper();

    private MangoToolApi() {}

    static void setService(ToolRegistrationService service) {
        MangoToolApi.toolRegistrationService = service;
    }

    private static void checkService() {
        if (toolRegistrationService == null) {
            throw new IllegalStateException("MangoToolApi has not been initialized yet.");
        }
    }

    /**
     * 注册工具类（无参数）
     * 
     * @param toolClass 工具类（应带有 @MangoTool 注解）
     * @return 工具配置 ID
     */
    public static Integer registerTool(Class<?> toolClass) {
        checkService();
        return toolRegistrationService.registerTool(toolClass, null, null);
    }

    /**
     * 注册工具类（带构造参数）
     * 
     * <p>参数会自动序列化为 JSON 存储到数据库。
     * 支持基本类型：String、集合、Map 等可 JSON 序列化的类型。
     * 
     * @param toolClass 工具类
     * @param args 构造参数（可变参数，自动序列化）
     * @return 工具配置 ID
     * @throws IllegalArgumentException 如果参数无法序列化
     */
    public static Integer registerTool(Class<?> toolClass, Object... args) {
        checkService();
        String argsJson = serializeArgs(args);
        return toolRegistrationService.registerTool(toolClass, argsJson, null);
    }

    /**
     * 注册工具类（来自插件，带构造参数）
     * 
     * @param toolClass 工具类
     * @param args 构造参数
     * @param pluginId 插件 ID
     * @return 工具配置 ID
     */
    public static Integer registerTool(Class<?> toolClass, Object[] args, Integer pluginId) {
        checkService();
        String argsJson = serializeArgs(args);
        return toolRegistrationService.registerTool(toolClass, argsJson, pluginId);
    }

    /**
     * 注册工具实例（直接传入实例，全局共享）
     * 
     * @param toolInstance 工具实例
     * @return 工具配置 ID
     */
    public static Integer registerToolInstance(Object toolInstance) {
        checkService();
        return toolRegistrationService.registerToolInstance(toolInstance, null);
    }

    /**
     * 注册工具实例（来自插件）
     * 
     * @param toolInstance 工具实例
     * @param pluginId 插件 ID
     * @return 工具配置 ID
     */
    public static Integer registerToolInstance(Object toolInstance, Integer pluginId) {
        checkService();
        return toolRegistrationService.registerToolInstance(toolInstance, pluginId);
    }

    /**
     * 注册工具工厂（每次创建 Agent 时新建实例）
     * 
     * @param toolClass 工具类
     * @param factory 工厂方法
     * @return 工具配置 ID
     */
    public static Integer registerToolFactory(Class<?> toolClass, Supplier<Object> factory) {
        checkService();
        return toolRegistrationService.registerToolFactory(toolClass, factory, null);
    }

    /**
     * 注册工具工厂（来自插件）
     * 
     * @param toolClass 工具类
     * @param factory 工厂方法
     * @param pluginId 插件 ID
     * @return 工具配置 ID
     */
    public static Integer registerToolFactory(Class<?> toolClass, Supplier<Object> factory, Integer pluginId) {
        checkService();
        return toolRegistrationService.registerToolFactory(toolClass, factory, pluginId);
    }

    /**
     * 注销工具类
     * 
     * @param toolClass 工具类
     */
    public static void unregisterTool(Class<?> toolClass) {
        checkService();
        toolRegistrationService.unregisterTool(toolClass.getName());
    }

    /**
     * 注销工具类（按类名）
     * 
     * @param className 类全限定名
     */
    public static void unregisterTool(String className) {
        checkService();
        toolRegistrationService.unregisterTool(className);
    }

    /**
     * 序列化构造参数
     */
    private static String serializeArgs(Object[] args) {
        if (args == null || args.length == 00) {
            return null;
        }
        try {
            return objectMapper.writeValueAsString(args);
        } catch (JsonProcessingException e) {
            throw new IllegalArgumentException("参数无法序列化: " + e.getMessage());
        }
    }
}
