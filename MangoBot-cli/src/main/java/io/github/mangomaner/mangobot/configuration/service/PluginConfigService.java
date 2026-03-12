package io.github.mangomaner.mangobot.configuration.service;

import com.baomidou.mybatisplus.extension.service.IService;
import io.github.mangomaner.mangobot.configuration.model.domain.PluginConfigEntity;
import io.github.mangomaner.mangobot.configuration.model.dto.plugin.UpdatePluginConfigRequest;
import io.github.mangomaner.mangobot.configuration.model.vo.PluginConfigVO;

import java.util.List;

/**
 * 插件配置服务接口
 */
public interface PluginConfigService extends IService<PluginConfigEntity> {

    /**
     * 获取所有插件配置（仅默认配置，bot_id 为 null）
     */
    List<PluginConfigVO> getAllConfigs();

    /**
     * 根据Bot ID获取配置（懒加载模式：返回默认配置和Bot专属配置，优先展示Bot专属配置）
     */
    List<PluginConfigVO> getConfigsByBotId(Long botId);

    /**
     * 根据插件ID获取配置列表（仅默认配置）
     */
    List<PluginConfigVO> getConfigsByPluginId(Long pluginId);

    /**
     * 根据插件ID和Bot ID获取配置列表（懒加载模式）
     */
    List<PluginConfigVO> getConfigsByPluginIdAndBotId(Long pluginId, Long botId);

    /**
     * 根据插件ID和配置Key获取配置（仅默认配置）
     */
    PluginConfigVO getConfig(Long pluginId, String configKey);

    /**
     * 根据插件ID、Bot ID和配置Key获取配置（优先Bot专属配置）
     */
    PluginConfigVO getConfig(Long pluginId, Long botId, String configKey);

    /**
     * 根据插件ID和配置Key获取配置值（仅默认配置）
     */
    String getConfigValue(Long pluginId, String configKey);

    /**
     * 根据插件ID、Bot ID和配置Key获取配置值（优先Bot专属配置）
     */
    String getConfigValue(Long pluginId, Long botId, String configKey);

    /**
     * 根据插件ID和配置Key获取配置值（带默认值，仅默认配置）
     */
    String getConfigValue(Long pluginId, String configKey, String defaultValue);

    /**
     * 注册插件配置（不发布事件，仅默认配置）
     */
    void registerConfig(Long pluginId, String configKey, String configValue, 
                        String configType, String description, String explain);

    /**
     * 更新插件配置
     */
    PluginConfigVO updateConfig(UpdatePluginConfigRequest request);

    /**
     * 根据插件ID和配置Key更新配置值（仅默认配置）
     */
    boolean updateConfigValue(Long pluginId, String configKey, String configValue);

    /**
     * 根据插件ID、Bot ID和配置Key更新配置值（懒加载：若Bot专属配置不存在则创建）
     */
    boolean updateConfigValue(Long pluginId, Long botId, String configKey, String configValue);

    /**
     * 删除插件的所有配置
     */
    void deleteByPluginId(Long pluginId);
}
