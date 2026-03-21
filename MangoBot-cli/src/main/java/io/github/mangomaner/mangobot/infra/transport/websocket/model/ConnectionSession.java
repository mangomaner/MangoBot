package io.github.mangomaner.mangobot.infra.transport.websocket.model;

import lombok.Getter;
import lombok.Setter;
import org.java_websocket.WebSocket;

import java.net.InetSocketAddress;

@Getter
public class ConnectionSession {

    private final String sessionId;
    private final Long configId;
    private final String protocolType;
    private final WebSocket connection;
    private final InetSocketAddress remoteAddress;
    
    @Setter
    private Long selfId;
    
    private long lastHeartbeatTime;
    
    @Setter
    private long heartbeatInterval = 60000;

    public ConnectionSession(WebSocket connection, Long configId, String protocolType) {
        this.connection = connection;
        this.configId = configId;
        this.protocolType = protocolType;
        this.remoteAddress = connection.getRemoteSocketAddress();
        this.sessionId = generateSessionId();
        this.lastHeartbeatTime = System.currentTimeMillis();
    }

    private String generateSessionId() {
        return configId + "_" + System.currentTimeMillis() + "_" + (int)(Math.random() * 10000);
    }

    public void updateHeartbeat() {
        this.lastHeartbeatTime = System.currentTimeMillis();
    }

    public boolean isConnected() {
        return connection.isOpen();
    }

    public void close() {
        try {
            if (connection.isOpen()) {
                connection.close();
            }
        } catch (Exception e) {
            // Ignore
        }
    }

    public void sendMessage(String message) {
        if (connection.isOpen()) {
            connection.send(message);
        }
    }
}
