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
     * 获取所有插件配置
     */
    List<PluginConfigVO> getAllConfigs();

    /**
     * 根据插件ID获取配置列表
     */
    List<PluginConfigVO> getConfigsByPluginId(Long pluginId);

    /**
     * 根据插件ID和配置Key获取配置
     */
    PluginConfigVO getConfig(Long pluginId, String configKey);

    /**
     * 根据插件ID和配置Key获取配置值
     */
    String getConfigValue(Long pluginId, String configKey);

    /**
     * 根据插件ID和配置Key获取配置值（带默认值）
     */
    String getConfigValue(Long pluginId, String configKey, String defaultValue);

    /**
     * 注册插件配置（不发布事件）
     */
    void registerConfig(Long pluginId, String configKey, String configValue, 
                        String configType, String description, String explain);

    /**
     * 更新插件配置
     */
    PluginConfigVO updateConfig(UpdatePluginConfigRequest request);

    /**
     * 根据插件ID和配置Key更新配置值
     */
    boolean updateConfigValue(Long pluginId, String configKey, String configValue);

    /**
     * 删除插件的所有配置
     */
    void deleteByPluginId(Long pluginId);
}
