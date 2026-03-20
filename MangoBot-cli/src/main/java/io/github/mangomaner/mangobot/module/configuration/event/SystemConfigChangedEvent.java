package io.github.mangomaner.mangobot.module.configuration.event;

import lombok.Getter;

/**
 * 系统配置变更事件
 * 当系统配置发生变更时发布此事件
 */
@Getter
public class SystemConfigChangedEvent extends ConfigurationEvent {

    private final Long configId;

    private final String configType;

    private final String category;

    public SystemConfigChangedEvent(Long configId, String configKey, String configType,
                                     String category, String oldValue, String newValue) {
        super(configKey, oldValue, newValue);
        this.configId = configId;
        this.configType = configType;
        this.category = category;
    }

    @Override
    public String getPostType() {
        return "system_config_event";
    }
}
