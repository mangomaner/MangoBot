package io.github.mangomaner.mangobot.agent.service.impl;

import io.agentscope.core.ReActAgent;
import io.agentscope.core.agent.StreamOptions;
import io.agentscope.core.message.Msg;
import io.github.mangomaner.mangobot.agent.factory.AgentFactory;
import io.github.mangomaner.mangobot.agent.hook.StreamingToolHook;
import io.github.mangomaner.mangobot.agent.model.dto.ChatMessageWebRequest;
import io.github.mangomaner.mangobot.agent.service.ChatMessageWebService;
import io.github.mangomaner.mangobot.agent.service.ChatService;
import io.github.mangomaner.mangobot.agent.service.ChatSessionService;
import io.github.mangomaner.mangobot.common.ErrorCode;
import io.github.mangomaner.mangobot.exception.BusinessException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Sinks;
import reactor.core.scheduler.Schedulers;

@Slf4j
@Service
@RequiredArgsConstructor
public class ChatServiceImpl implements ChatService {

    private final AgentFactory agentFactory;
    private final StreamingToolHook streamingToolHook;
    private final ChatMessageWebService chatMessageWebService;
    private final ChatSessionService chatSessionService;
    private final io.github.mangomaner.mangobot.agent.manager.MemoryManager memoryManager;

    private static final String ROLE_USER = "user";

    @Override
    public Flux<String> streamChat(Integer sessionId, String message) {
        if (sessionId == null) {
            return Flux.just("<Error>会话ID不能为空</Error>\n[DONE]");
        }
        if (!StringUtils.hasText(message)) {
            return Flux.just("<Error>消息内容不能为空</Error>\n");
        }

        try {
            chatSessionService.getSessionById(sessionId);
        } catch (Exception e) {
            return Flux.just("<Error>会话不存在</Error>\n");
        }

        Sinks.Many<String> sink = Sinks.many().multicast().onBackpressureBuffer();

        ReActAgent agent = agentFactory.createAgent(sessionId);

        if(agent.getModel() == null) {
            return Flux.just("<Error>主模型未配置</Error>\n");
        }

        String agentName = agent.getName();

        streamingToolHook.registerSession(agentName, sink, sessionId);

        persistUserMessage(sessionId, message);

        Msg userMsg = Msg.builder().textContent(message).build();

        StreamOptions options = StreamOptions.defaults();

        agent.stream(userMsg, options)
                .subscribeOn(Schedulers.boundedElastic())
                .doOnComplete(() -> {
                    sink.tryEmitNext("[DONE]");
                    // 只有成功才加入上下文
                    memoryManager.persistAndRemoveMemory(sessionId);
                    log.info("Stream completed for session: {}", sessionId);
                })
                .doOnError(error -> {
                    log.error("Stream error for session {}: {}", sessionId, error.getMessage(), error);
                    sink.tryEmitNext("\n<Error>" + error.getMessage() + "</Error>\n");
                })
                .doFinally(signalType -> {
                    streamingToolHook.unregisterSession(agentName);
                    sink.tryEmitComplete();
                })
                .subscribe();

        return sink.asFlux();
    }

    private void persistUserMessage(Integer sessionId, String content) {
        try {
            chatMessageWebService.createMessage(new ChatMessageWebRequest() {{
                setSessionId(sessionId);
                setContent(content);
                setRole(ROLE_USER);
                setMetadata("{}");
            }});
            log.info("Persisted user message for session: {}", sessionId);
        } catch (Exception e) {
            log.error("Failed to persist user message for session: {}", sessionId, e);
        }
    }
}
