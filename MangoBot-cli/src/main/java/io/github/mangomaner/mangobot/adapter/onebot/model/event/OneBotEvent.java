package io.github.mangomaner.mangobot.adapter.onebot.model.event;

/**
 * Event interface for all OneBot events.
 */
public interface OneBotEvent {
    long getTime();
    long getSelfId();
    String getPostType();
}
