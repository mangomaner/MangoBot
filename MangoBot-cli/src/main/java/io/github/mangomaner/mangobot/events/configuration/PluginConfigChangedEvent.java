package io.github.mangomaner.mangobot.events.configuration;

import io.github.mangomaner.mangobot.module.configuration.event.ConfigurationEvent;
import lombok.Getter;

/**
 * 插件配置变更事件
 * 当插件配置发生变更时发布此事件
 */
@Getter
public class PluginConfigChangedEvent extends ConfigurationEvent {

    private final Long configId;

    private final Long pluginId;

    private final String pluginName;

    private final String configType;

    public PluginConfigChangedEvent(Long configId, Long pluginId, String pluginName,
                                     String configKey, String configType, 
                                     String oldValue, String newValue) {
        super(configKey, oldValue, newValue);
        this.configId = configId;
        this.pluginId = pluginId;
        this.pluginName = pluginName;
        this.configType = configType;
    }

    @Override
    public String getPostType() {
        return "plugin_config_event";
    }
}
