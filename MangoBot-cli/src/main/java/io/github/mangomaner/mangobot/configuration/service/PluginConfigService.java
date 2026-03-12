package io.github.mangomaner.mangobot.configuration.service;

import com.baomidou.mybatisplus.extension.service.IService;
import io.github.mangomaner.mangobot.configuration.model.config.PluginConfigDefinition;
import io.github.mangomaner.mangobot.configuration.model.domain.PluginConfigEntity;
import io.github.mangomaner.mangobot.configuration.model.dto.plugin.UpdatePluginConfigRequest;
import io.github.mangomaner.mangobot.configuration.model.vo.PluginConfigVO;

import java.util.List;

public interface PluginConfigService extends IService<PluginConfigEntity> {

    List<PluginConfigVO> getAllConfigs();

    List<PluginConfigVO> getConfigsByBotId(Long botId);

    List<PluginConfigVO> getConfigsByPluginId(Long pluginId);

    List<PluginConfigVO> getConfigsByPluginIdAndBotId(Long pluginId, Long botId);

    PluginConfigVO getConfig(Long pluginId, String configKey);

    PluginConfigVO getConfig(Long pluginId, Long botId, String configKey);

    String getConfigValue(Long pluginId, String configKey);

    String getConfigValue(Long pluginId, Long botId, String configKey);

    String getConfigValue(Long pluginId, String configKey, String defaultValue);

    void registerConfig(Long pluginId, String configKey, String configValue, 
                        String configType, String description, String explain);

    void registerDefinition(Long pluginId, PluginConfigDefinition definition);

    void registerDefinitions(Long pluginId, List<PluginConfigDefinition> definitions);

    PluginConfigVO updateConfig(UpdatePluginConfigRequest request);

    boolean updateConfigValue(Long pluginId, String configKey, String configValue);

    boolean updateConfigValue(Long pluginId, Long botId, String configKey, String configValue);

    void deleteByPluginId(Long pluginId);
}
