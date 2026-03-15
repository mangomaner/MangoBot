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
            return Flux.error(new BusinessException(ErrorCode.PARAMS_ERROR, "会话ID不能为空"));
        }
        if (!StringUtils.hasText(message)) {
            return Flux.error(new BusinessException(ErrorCode.PARAMS_ERROR, "消息内容不能为空"));
        }

        try {
            chatSessionService.getSessionById(sessionId);
        } catch (Exception e) {
            return Flux.error(new BusinessException(ErrorCode.PARAMS_ERROR, "会话不存在"));
        }

        Sinks.Many<String> sink = Sinks.many().multicast().onBackpressureBuffer();

        ReActAgent agent = agentFactory.createAgent(sessionId);
        String agentName = agent.getName();

        streamingToolHook.registerSession(agentName, sink, sessionId);

        persistUserMessage(sessionId, message);

        Msg userMsg = Msg.builder().textContent(message).build();

        StreamOptions options = StreamOptions.defaults();

        agent.stream(userMsg, options)
                .subscribeOn(Schedulers.boundedElastic())
                .doOnComplete(() -> {
                    sink.tryEmitNext("[DONE]");
                    log.info("Stream completed for session: {}", sessionId);
                })
                .doOnError(error -> {
                    log.error("Stream error for session {}: {}", sessionId, error.getMessage(), error);
                    sink.tryEmitNext("\n<Error>" + error.getMessage() + "</Error>\n");
                })
                .doFinally(signalType -> {
                    streamingToolHook.unregisterSession(agentName);
                    memoryManager.persistAndRemoveMemory(sessionId);
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
