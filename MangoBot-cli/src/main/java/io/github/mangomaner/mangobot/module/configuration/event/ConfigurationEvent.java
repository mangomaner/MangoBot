package io.github.mangomaner.mangobot.module.configuration.event;

import io.github.mangomaner.mangobot.adapter.onebot.event.OneBotEvent;
import lombok.Getter;

/**
 * 配置事件基类
 * 所有配置变更事件的父类
 */
@Getter
public abstract class ConfigurationEvent implements OneBotEvent {

    protected final String configKey;

    protected final String oldValue;

    protected final String newValue;

    protected final long time;

    public ConfigurationEvent(String configKey, String oldValue, String newValue) {
        this.configKey = configKey;
        this.oldValue = oldValue;
        this.newValue = newValue;
        this.time = System.currentTimeMillis();
    }

    @Override
    public long getSelfId() {
        return -1;
    }

    @Override
    public String getPostType() {
        return "config_event";
    }
}
