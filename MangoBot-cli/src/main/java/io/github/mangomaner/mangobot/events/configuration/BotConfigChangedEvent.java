package io.github.mangomaner.mangobot.events.configuration;

import io.github.mangomaner.mangobot.module.configuration.event.ConfigurationEvent;
import lombok.Getter;

/**
 * Bot 配置变更事件
 * 当 Bot 配置发生变更时发布此事件
 */
@Getter
public class BotConfigChangedEvent extends ConfigurationEvent {

    private final Long configId;

    private final String botId;

    private final String configType;

    private final String category;

    public BotConfigChangedEvent(Long configId, String botId, String configKey, String configType,
                                  String category, String oldValue, String newValue) {
        super(configKey, oldValue, newValue);
        this.configId = configId;
        this.botId = botId;
        this.configType = configType;
        this.category = category;
    }

    @Override
    public String getPostType() {
        return "bot_config_event";
    }
}
