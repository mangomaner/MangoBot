package io.github.mangomaner.mangobot.infra.transport.websocket;

import io.github.mangomaner.mangobot.adapter.connect_controller.onebot.model.domain.OneBotConfig;
import io.github.mangomaner.mangobot.adapter.connect_controller.onebot.model.enums.ConnectionStatus;
import io.github.mangomaner.mangobot.system.common.ErrorCode;
import io.github.mangomaner.mangobot.infra.transport.websocket.model.ConnectionSession;
import io.github.mangomaner.mangobot.system.exception.BusinessException;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.java_websocket.WebSocket;
import org.java_websocket.handshake.ClientHandshake;
import org.java_websocket.server.WebSocketServer;
import org.springframework.stereotype.Component;

import java.net.InetSocketAddress;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
@Slf4j
public class WebSocketServerManager {

    private static final int CLOSE_POLICY_VIOLATION = 1008;
    private static final Map<Long, ServerInstance> runningServers = new ConcurrentHashMap<>();

    private final MessageDispatcher messageDispatcher;

    public WebSocketServerManager(MessageDispatcher messageDispatcher) {
        this.messageDispatcher = messageDispatcher;
    }

    public synchronized void startServer(OneBotConfig config) throws Exception {
        Long configId = config.getId();
        
        if (runningServers.containsKey(configId)) {
            log.warn("WebSocket 服务器已在运行中，配置ID: {}", configId);
            return;
        }

        String host = config.getHost();
        int port = config.getPort();
        String protocolType = config.getProtocolType();

        if (protocolType ==  null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "协议类型不能为空");
        }

        log.info("正在启动 WebSocket 服务器: {}:{}, 协议: {}", host, port, protocolType);

        ServerInstance instance = new ServerInstance(config, messageDispatcher);
        instance.start();
        runningServers.put(configId, instance);

        log.info("WebSocket 服务器启动成功: {}:{}, 协议: {}", host, port, protocolType);
    }

    public synchronized void stopServer(Long configId) {
        ServerInstance instance = runningServers.remove(configId);
        if (instance != null) {
            try {
                instance.stop();
                log.info("WebSocket 服务器已停止，配置ID: {}", configId);
            } catch (Exception e) {
                log.error("停止 WebSocket 服务器失败，配置ID: {}", configId, e);
            }
        }
    }

    public boolean isRunning(Long configId) {
        ServerInstance instance = runningServers.get(configId);
        return instance != null && instance.isRunning();
    }

    public ConnectionStatus getStatus(Long configId) {
        ServerInstance instance = runningServers.get(configId);
        if (instance == null) {
            return ConnectionStatus.STOPPED;
        }
        return instance.isRunning() ? ConnectionStatus.RUNNING : ConnectionStatus.ERROR;
    }

    public void stopAll() {
        runningServers.keySet().forEach(this::stopServer);
    }

    @Getter
    private static class ServerInstance {
        private final OneBotConfig config;
        private final WebSocketServer server;
        private final MessageDispatcher messageDispatcher;
        private final Map<WebSocket, ConnectionSession> sessions = new ConcurrentHashMap<>();
        private volatile boolean running = false;

        public ServerInstance(OneBotConfig config, MessageDispatcher messageDispatcher) {
            this.config = config;
            this.messageDispatcher = messageDispatcher;
            this.server = createWebSocketServer(config);
        }

        private WebSocketServer createWebSocketServer(OneBotConfig config) {
            InetSocketAddress address = new InetSocketAddress(config.getHost(), config.getPort());
            String protocolType = config.getProtocolType() != null ? config.getProtocolType() : "onebot_qq";
            
            return new WebSocketServer(address) {

                @Override
                public void onOpen(WebSocket conn, ClientHandshake handshake) {
                    log.info("WebSocket 连接建立: {}", conn.getRemoteSocketAddress());
                    
                    String resourceDescriptor = handshake.getResourceDescriptor();
                    if (config.getToken() != null && !config.getToken().isEmpty()) {
                        if (!validateToken(resourceDescriptor, config.getToken())) {
                            log.warn("Token 验证失败，关闭连接: {}", conn.getRemoteSocketAddress());
                            conn.close(CLOSE_POLICY_VIOLATION, "Invalid token");
                            return;
                        }
                    }
                    
                    ConnectionSession session = new ConnectionSession(conn, config.getId(), protocolType);
                    sessions.put(conn, session);
                    messageDispatcher.onConnect(session);
                }

                @Override
                public void onClose(WebSocket conn, int code, String reason, boolean remote) {
                    log.info("WebSocket 连接关闭: {}, code: {}, reason: {}", 
                            conn.getRemoteSocketAddress(), code, reason);
                    
                    ConnectionSession session = sessions.remove(conn);
                    if (session != null) {
                        messageDispatcher.onDisconnect(session);
                    }
                }

                @Override
                public void onMessage(WebSocket conn, String message) {
                    log.debug("收到 WebSocket 消息: {}", message);
                    
                    ConnectionSession session = sessions.get(conn);
                    if (session != null) {
                        messageDispatcher.dispatch(session, message);
                    }
                }

                @Override
                public void onError(WebSocket conn, Exception ex) {
                    log.error("WebSocket 错误: {}", conn != null ? conn.getRemoteSocketAddress() : "unknown", ex);
                }

                @Override
                public void onStart() {
                    log.info("WebSocket 服务器启动完成: {}:{}", config.getHost(), config.getPort());
                    running = true;
                }

                private boolean validateToken(String resourceDescriptor, String expectedToken) {
                    if (resourceDescriptor == null || resourceDescriptor.isEmpty()) {
                        return expectedToken == null || expectedToken.isEmpty();
                    }
                    
                    if (resourceDescriptor.contains("access_token=" + expectedToken)) {
                        return true;
                    }
                    
                    if (resourceDescriptor.endsWith("/" + expectedToken)) {
                        return true;
                    }
                    
                    return false;
                }
            };
        }

        public void start() throws Exception {
            server.start();
        }

        public void stop() throws Exception {
            running = false;
            server.stop();
        }

        public boolean isRunning() {
            return running;
        }
    }
}
