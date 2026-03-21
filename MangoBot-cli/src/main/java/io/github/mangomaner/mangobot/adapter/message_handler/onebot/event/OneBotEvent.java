package io.github.mangomaner.mangobot.adapter.message_handler.onebot.event;

/**
 * Event interface for all OneBot events.
 */
public interface OneBotEvent {
    long getTime();
    long getSelfId();
    String getPostType();
}
