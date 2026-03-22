package io.github.mangomaner.mangobot.infra.websocket;

import io.github.mangomaner.mangobot.adapter.WebSocketProtocolAdapter;
import io.github.mangomaner.mangobot.infra.websocket.model.ConnectionSession;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Component
@Slf4j
public class MessageDispatcher {

    private final Map<String, WebSocketProtocolAdapter> adapterMap;

    public MessageDispatcher(List<WebSocketProtocolAdapter> adapters) {
        this.adapterMap = adapters.stream()
                .collect(Collectors.toConcurrentMap(
                        WebSocketProtocolAdapter::getProtocolType,
                        Function.identity()
                ));
        log.info("已加载 {} 个协议适配器: {}", adapterMap.size(), adapterMap.keySet());
    }

    public WebSocketProtocolAdapter getAdapter(String protocolType) {
        return adapterMap.get(protocolType);
    }

    public void dispatch(ConnectionSession session, String message) {
        WebSocketProtocolAdapter adapter = adapterMap.get(session.getProtocolType());
        if (adapter == null) {
            log.warn("未找到协议适配器: protocolType={}, sessionId={}", 
                    session.getProtocolType(), session.getSessionId());
            return;
        }
        
        try {
            adapter.onMessage(session, message);
        } catch (Exception e) {
            log.error("消息处理异常: protocolType={}, sessionId={}", 
                    session.getProtocolType(), session.getSessionId(), e);
        }
    }

    public void onConnect(ConnectionSession session) {
        WebSocketProtocolAdapter adapter = adapterMap.get(session.getProtocolType());
        if (adapter != null) {
            try {
                adapter.onConnect(session);
            } catch (Exception e) {
                log.error("连接处理异常: protocolType={}, sessionId={}", 
                        session.getProtocolType(), session.getSessionId(), e);
            }
        }
    }

    public void onDisconnect(ConnectionSession session) {
        WebSocketProtocolAdapter adapter = adapterMap.get(session.getProtocolType());
        if (adapter != null) {
            try {
                adapter.onDisconnect(session);
            } catch (Exception e) {
                log.error("断开处理异常: protocolType={}, sessionId={}", 
                        session.getProtocolType(), session.getSessionId(), e);
            }
        }
    }
}
