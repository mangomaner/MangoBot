package io.github.mangomaner.mangobot.configuration.event;

import lombok.Getter;

/**
 * 模型配置变更事件
 * 当模型配置发生变更时发布此事件
 */
@Getter
public class ModelConfigChangedEvent extends ConfigurationEvent {

    private final Long modelConfigId;

    private final String modelName;

    private final Long providerId;

    public ModelConfigChangedEvent(Long modelConfigId, String modelName, 
                                    Long providerId, String oldValue, String newValue) {
        super("model_config_" + modelConfigId, oldValue, newValue);
        this.modelConfigId = modelConfigId;
        this.modelName = modelName;
        this.providerId = providerId;
    }

    @Override
    public String getPostType() {
        return "model_config_event";
    }
}
