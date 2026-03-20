package io.github.mangomaner.mangobot.module.agent.hook;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.agentscope.core.hook.*;
import io.agentscope.core.message.Msg;
import io.agentscope.core.message.TextBlock;
import io.agentscope.core.message.ToolUseBlock;
import io.github.mangomaner.mangobot.module.agent.model.vo.TokenUsageVO;
import io.github.mangomaner.mangobot.module.agent.service.ChatMessageWebService;
import io.github.mangomaner.mangobot.utils.TokenUsageUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;
import reactor.core.publisher.Sinks;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;

@Slf4j
@Component
@RequiredArgsConstructor
public class StreamingToolHook implements Hook {

    private final ChatMessageWebService chatMessageWebService;

    private final ConcurrentHashMap<String, Sinks.Many<String>> sessionSinks = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, StringBuilder> responseBuffers = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, Integer> sessionIdMapping = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, AtomicBoolean> recordedFlags = new ConcurrentHashMap<>();

    public void registerSession(String agentName, Sinks.Many<String> sink, Integer chatSessionId) {
        sessionSinks.put(agentName, sink);
        responseBuffers.put(agentName, new StringBuilder());
        sessionIdMapping.put(agentName, chatSessionId);
        recordedFlags.put(agentName, new AtomicBoolean(false));
        log.debug("Registered streaming session: {}, chatSessionId: {}", agentName, chatSessionId);
    }

    public void unregisterSession(String agentName) {
        persistResponseIfNeeded(agentName);
        sessionSinks.remove(agentName);
        responseBuffers.remove(agentName);
        sessionIdMapping.remove(agentName);
        recordedFlags.remove(agentName);
        log.debug("Unregistered streaming session: {}", agentName);
    }

    private void emit(String agentName, String message) {
        StringBuilder buffer = responseBuffers.get(agentName);
        if (buffer != null) {
            buffer.append(message);
        }
        Sinks.Many<String> sink = sessionSinks.get(agentName);
        if (sink != null) {
            // 将换行符替换为特殊字符 \u0000，避免 SSE 解析问题
            String escapedMessage = message.replace("\n", "\u0000");
            sink.tryEmitNext(escapedMessage);
        }
    }

    private void persistResponseIfNeeded(String agentName) {
        AtomicBoolean recorded = recordedFlags.get(agentName);
        if (recorded == null || recorded.get()) {
            return;
        }

        StringBuilder buffer = responseBuffers.get(agentName);
        Integer chatSessionId = sessionIdMapping.get(agentName);

        if (buffer != null && buffer.length() > 0 && chatSessionId != null) {
            String content = buffer.toString().trim();
            if (!content.isEmpty()) {
                try {
                    chatMessageWebService.createAssistantMessage(chatSessionId, content, "{}");
                    recorded.set(true);
                    log.debug("Persisted AI response for session: {}, length: {}", agentName, content.length());
                } catch (Exception e) {
                    log.error("Failed to persist AI response for session: {}", agentName, e);
                }
            }
        }
    }

    @Override
    public <T extends HookEvent> Mono<T> onEvent(T event) {
        String agentName = event.getAgent() != null ? event.getAgent().getName() : "default";

        if (event instanceof PreCallEvent) {
            log.debug("[Agent] 开始调用: {}", agentName);
        }
        else if (event instanceof PreReasoningEvent) {
            log.debug("[Reasoning] 开始推理: {}", agentName);
        }
        else if (event instanceof ReasoningChunkEvent e) {
            Msg chunk = e.getIncrementalChunk();
            if (chunk != null) {
                String content = chunk.getTextContent();
                if (content != null) {
                    log.debug("[ReasoningChunk] {}", content);
                    emit(agentName, content);
                }
            }
        }
        else if (event instanceof PostReasoningEvent) {
            log.debug("[Reasoning] 推理完成: {}", agentName);
        }
        else if (event instanceof PreActingEvent e) {
            ToolUseBlock toolUse = e.getToolUse();
            log.info("[Tool] 调用: {} 参数: {}", toolUse.getName(), toolUse.getInput());
            emit(agentName, "<FunctionCall>" + toolUse.getName() + "</FunctionCall>\n");
        }
        else if (event instanceof PostActingEvent e) {
            String result = e.getToolResult().getOutput().stream()
                    .filter(block -> block instanceof TextBlock)
                    .map(block -> ((TextBlock) block).getText())
                    .findFirst()
                    .orElse("无文本结果");
            log.info("[Tool] 执行结果: {}", result);
            emit(agentName, "<FunctionCallResult>" + result + "</FunctionCallResult>\n");
        }
        else if (event instanceof PostCallEvent e) {
            log.info("[Agent] 调用完成: {}", agentName);
            // 获取并发送 Token 用量信息
            emitTokenUsage(agentName, e);
            persistResponseIfNeeded(agentName);
        }
        else if (event instanceof ErrorEvent e) {
            log.error("[Error] 执行错误: {}", e.getError().getMessage(), e.getError());
            emit(agentName, "\n<Error>" + e.getError().getMessage() + "</Error>");
            persistResponseIfNeeded(agentName);
        }

        return Mono.just(event);
    }

    /**
     * 获取并发送 Token 用量信息
     * <p>
     * 从 PostCallEvent 中获取最终消息的 ChatUsage，转换为 TokenUsageVO 后发送给前端
     *
     * @param agentName Agent 名称
     * @param event     PostCallEvent 事件
     */
    private void emitTokenUsage(String agentName, PostCallEvent event) {
        try {
            // 使用工具类提取 Token 用量
            TokenUsageVO tokenUsageVO = TokenUsageUtils.extractFromEvent(event);
            if (tokenUsageVO == null) {
                return;
            }

            // 序列化为 JSON 并发送
            ObjectMapper mapper = new ObjectMapper();
            String json = mapper.writeValueAsString(tokenUsageVO);
            emit(agentName, "<TokenUsage>" + json + "</TokenUsage>\n");
            log.info("[TokenUsage] 输入: {} tokens, 输出: {} tokens, 耗时: {}s",
                    tokenUsageVO.getInputTokens(),
                    tokenUsageVO.getOutputTokens(),
                    tokenUsageVO.getTime());
        } catch (JsonProcessingException e) {
            log.error("[TokenUsage] JSON 序列化失败", e);
        } catch (Exception e) {
            log.error("[TokenUsage] 获取 Token 用量失败", e);
        }
    }
}
