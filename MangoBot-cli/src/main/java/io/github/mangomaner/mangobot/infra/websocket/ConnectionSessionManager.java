package io.github.mangomaner.mangobot.infra.websocket;

import io.github.mangomaner.mangobot.infra.websocket.model.ConnectionSession;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
@Slf4j
public class ConnectionSessionManager {

    private final Map<String, ConnectionSession> sessionsBySessionId = new ConcurrentHashMap<>();
    private final Map<Long, ConnectionSession> sessionsBySelfId = new ConcurrentHashMap<>();

    public void registerSession(ConnectionSession session) {
        sessionsBySessionId.put(session.getSessionId(), session);
        log.debug("注册新连接: sessionId={}, configId={}, protocol={}", 
                session.getSessionId(), session.getConfigId(), session.getProtocolType());
    }

    public void registerSelfId(ConnectionSession session, Long selfId) {
        session.setSelfId(selfId);
        sessionsBySelfId.put(selfId, session);
        log.info("连接已绑定: selfId={}, sessionId={}", selfId, session.getSessionId());
    }

    public void removeSession(ConnectionSession session) {
        sessionsBySessionId.remove(session.getSessionId());
        if (session.getSelfId() != null) {
            sessionsBySelfId.remove(session.getSelfId());
        }
        log.debug("移除连接: sessionId={}, selfId={}", session.getSessionId(), session.getSelfId());
    }

    public ConnectionSession getSessionBySessionId(String sessionId) {
        return sessionsBySessionId.get(sessionId);
    }

    public ConnectionSession getSessionBySelfId(Long selfId) {
        return sessionsBySelfId.get(selfId);
    }

    public List<Long> getAllSelfIds() {
        return sessionsBySelfId.keySet().stream().toList();
    }

    public void updateHeartbeat(ConnectionSession session) {
        session.updateHeartbeat();
    }

    public void updateHeartbeat(ConnectionSession session, long interval) {
        session.updateHeartbeat();
        if (interval > 0) {
            session.setHeartbeatInterval(interval);
        }
    }

    @Scheduled(fixedRate = 10000)
    public void checkHeartbeats() {
        long now = System.currentTimeMillis();
        sessionsBySessionId.values().forEach(session -> {
            long timeout = session.getHeartbeatInterval() * 3;
            if (now - session.getLastHeartbeatTime() > timeout && timeout > 0) {
                log.warn("Session {} (selfId={}) 超时 (上次心跳在 {} ms 前). 关闭连接.",
                        session.getSessionId(), session.getSelfId(), now - session.getLastHeartbeatTime());
                session.close();
            }
        });
    }
}
