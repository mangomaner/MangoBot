package io.github.mangomaner.mangobot.api;

import io.github.mangomaner.mangobot.module.configuration.service.PluginConfigService;
import io.github.mangomaner.mangobot.module.configuration.service.SystemConfigService;
import io.github.mangomaner.mangobot.module.configuration.model.vo.PluginConfigVO;
import io.github.mangomaner.mangobot.module.configuration.model.vo.SystemConfigVO;
import io.github.mangomaner.mangobot.plugin.core.PluginClassLoader;

/**
 * 配置 API (静态工具类)
 * 
 * <p>提供系统配置和插件配置的统一访问能力。
 * 
 * <h3>权限控制</h3>
 * <ul>
 *   <li>系统配置：所有调用者可读，仅主程序可修改（插件调用修改方法将抛出异常）</li>
 *   <li>插件配置：插件只能读取和修改自己的配置</li>
 * </ul>
 * 
 * <h3>插件自动识别</h3>
 * <p>当插件调用此 API 时，会自动检测调用者所属的插件 ID，
 * 无需手动传入。检测机制：
 * <ol>
 *   <li>优先检查当前线程的 ContextClassLoader</li>
 *   <li>其次分析调用栈中所有类的 ClassLoader</li>
 * </ol>
 */
public class MangoConfigApi {

    private static SystemConfigService systemConfigService;
    private static PluginConfigService pluginConfigService;

    private MangoConfigApi() {}

    static void setSystemConfigService(SystemConfigService service) {
        MangoConfigApi.systemConfigService = service;
    }

    static void setPluginConfigService(PluginConfigService service) {
        MangoConfigApi.pluginConfigService = service;
    }

    private static void checkServices() {
        if (systemConfigService == null || pluginConfigService == null) {
            throw new IllegalStateException("MangoConfigApi has not been initialized yet.");
        }
    }

    /**
     * 自动检测当前调用者的插件 ID
     * 
     * @return 插件数据库 ID，如果非插件调用则返回 null
     */
    private static Integer detectPluginId() {
        ClassLoader loader = Thread.currentThread().getContextClassLoader();
        if (loader instanceof PluginClassLoader pcl) {
            Long id = pcl.getPluginDbId();
            return id != null ? id.intValue() : null;
        }
        
        return detectPluginIdFromStackTrace();
    }

    /**
     * 从调用栈中检测插件 ID
     */
    private static Integer detectPluginIdFromStackTrace() {
        StackTraceElement[] stackTrace = Thread.currentThread().getStackTrace();
        
        for (StackTraceElement element : stackTrace) {
            try {
                Class<?> clazz = Class.forName(element.getClassName());
                ClassLoader loader = clazz.getClassLoader();
                
                if (loader instanceof PluginClassLoader pcl) {
                    Long id = pcl.getPluginDbId();
                    if (id != null) {
                        return id.intValue();
                    }
                }
            } catch (ClassNotFoundException ignored) {
            }
        }
        
        return null;
    }

    // ==================== 系统配置（所有人可读） ====================

    /**
     * 根据Key和Bot ID获取系统配置（优先返回Bot专属配置，其次返回默认配置）
     *
     * @param configKey 配置键
     * @param botId     Bot ID
     * @return 系统配置视图对象，如果不存在则返回 null
     */
    public static SystemConfigVO getSystemConfig(String configKey, Long botId) {
        checkServices();
        return systemConfigService.getConfigByKeyAndBotId(configKey, botId);
    }

    /**
     * 根据Key和Bot ID获取系统配置值（优先Bot专属配置）
     *
     * @param configKey 配置键
     * @param botId     Bot ID
     * @return 配置值，如果不存在则返回 null
     */
    public static String getSystemConfigValue(String configKey, Long botId) {
        checkServices();
        return systemConfigService.getConfigValue(configKey, botId);
    }

    /**
     * 根据Key和Bot ID获取系统配置值（带默认值）
     *
     * @param configKey    配置键
     * @param botId        Bot ID
     * @param defaultValue 默认值
     * @return 配置值，如果不存在则返回默认值
     */
    public static String getSystemConfigValue(String configKey, Long botId, String defaultValue) {
        checkServices();
        String value = systemConfigService.getConfigValue(configKey, botId);
        return value != null ? value : defaultValue;
    }

    /**
     * 根据Key和Bot ID更新系统配置值
     * 
     * <p>若Bot专属配置不存在，会将所有默认配置复制为Bot专属配置后再更新。
     * <p><b>权限限制：</b>仅主程序可调用，插件调用将抛出 SecurityException
     *
     * @param configKey   配置键
     * @param botId       Bot ID
     * @param configValue 配置值
     * @return 是否更新成功
     * @throws SecurityException 如果插件调用此方法
     */
    public static boolean updateSystemConfigValue(String configKey, Long botId, String configValue) {
        checkServices();
        assertNotPlugin("updateSystemConfigValue");
        return systemConfigService.updateConfigValue(configKey, botId, configValue);
    }

    // ==================== 插件配置（仅自己的配置） ====================

    /**
     * 获取当前插件的配置
     * 
     * <p><b>权限限制：</b>仅插件可调用，主程序调用将抛出 IllegalStateException
     *
     * @param configKey 配置键
     * @param botId     Bot ID
     * @return 插件配置视图对象，如果不存在则返回 null
     * @throws IllegalStateException 如果主程序调用此方法
     */
    public static PluginConfigVO getPluginConfig(String configKey, Long botId) {
        checkServices();
        Integer pluginId = assertPlugin("getPluginConfig");
        return pluginConfigService.getConfig(pluginId.longValue(), botId, configKey);
    }

    /**
     * 获取当前插件的配置值
     * 
     * <p><b>权限限制：</b>仅插件可调用，主程序调用将抛出 IllegalStateException
     *
     * @param configKey 配置键
     * @param botId     Bot ID
     * @return 配置值，如果不存在则返回 null
     * @throws IllegalStateException 如果主程序调用此方法
     */
    public static String getPluginConfigValue(String configKey, Long botId) {
        checkServices();
        Integer pluginId = assertPlugin("getPluginConfigValue");
        return pluginConfigService.getConfigValue(pluginId.longValue(), botId, configKey);
    }

    /**
     * 获取当前插件的配置值（带默认值）
     * 
     * <p><b>权限限制：</b>仅插件可调用，主程序调用将抛出 IllegalStateException
     *
     * @param configKey    配置键
     * @param botId        Bot ID
     * @param defaultValue 默认值
     * @return 配置值，如果不存在则返回默认值
     * @throws IllegalStateException 如果主程序调用此方法
     */
    public static String getPluginConfigValue(String configKey, Long botId, String defaultValue) {
        checkServices();
        Integer pluginId = assertPlugin("getPluginConfigValue");
        String value = pluginConfigService.getConfigValue(pluginId.longValue(), botId, configKey);
        return value != null ? value : defaultValue;
    }

    /**
     * 更新当前插件的配置值
     * 
     * <p>若Bot专属配置不存在，会将所有默认配置复制为Bot专属配置后再更新。
     * <p><b>权限限制：</b>仅插件可调用，主程序调用将抛出 IllegalStateException
     *
     * @param configKey   配置键
     * @param botId       Bot ID
     * @param configValue 配置值
     * @return 是否更新成功
     * @throws IllegalStateException 如果主程序调用此方法
     */
    public static boolean updatePluginConfigValue(String configKey, Long botId, String configValue) {
        checkServices();
        Integer pluginId = assertPlugin("updatePluginConfigValue");
        return pluginConfigService.updateConfigValue(pluginId.longValue(), botId, configKey, configValue);
    }

    // ==================== 权限检查辅助方法 ====================

    /**
     * 断言当前调用者不是插件
     * 
     * @param methodName 方法名（用于错误信息）
     * @throws SecurityException 如果当前调用者是插件
     */
    private static void assertNotPlugin(String methodName) {
        Integer pluginId = detectPluginId();
        if (pluginId != null) {
            throw new SecurityException(
                "Plugin (ID: " + pluginId + ") is not allowed to call " + methodName + 
                ". System config modification is restricted to main program only."
            );
        }
    }

    /**
     * 断言当前调用者是插件并返回插件ID
     * 
     * @param methodName 方法名（用于错误信息）
     * @return 插件ID
     * @throws IllegalStateException 如果当前调用者不是插件
     */
    private static Integer assertPlugin(String methodName) {
        Integer pluginId = detectPluginId();
        if (pluginId == null) {
            throw new IllegalStateException(
                methodName + " can only be called by plugins. Main program should use PluginConfigService directly."
            );
        }
        return pluginId;
    }
}
