package io.github.mangomaner.mangobot.adapter;

import io.github.mangomaner.mangobot.infra.websocket.model.ConnectionSession;

public interface WebSocketProtocolAdapter {

    String getProtocolType();

    void onMessage(ConnectionSession session, String message);

    void onConnect(ConnectionSession session);

    void onDisconnect(ConnectionSession session);
}
