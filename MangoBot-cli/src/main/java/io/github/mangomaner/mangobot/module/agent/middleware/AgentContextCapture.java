package io.github.mangomaner.mangobot.module.agent.middleware;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.agentscope.core.agent.Agent;
import io.agentscope.core.agent.RuntimeContext;
import io.agentscope.core.event.AgentEvent;
import io.agentscope.core.event.ModelCallEndEvent;
import io.agentscope.core.middleware.MiddlewareBase;
import io.agentscope.core.middleware.ModelCallInput;
import io.agentscope.core.model.ToolSchema;
import io.github.mangomaner.mangobot.api.context.ChatContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;

/**
 * 模型调用上下文采集（供调试接口按需查看，不打日志）
 *
 * <p>每次 LLM 调用把"发送给模型的完整上下文"（消息列表 + 工具 schema 名 + Token 用量）
 * 按会话 ID 暂存到内存，通过 {@code GET /api/agent/debug/context?sessionId=} 查看，
 * 用于分析每次对话的一万多 input token 花在哪里。
 */
@Slf4j
@Component
public class AgentContextCapture implements MiddlewareBase {

    private final ObjectMapper objectMapper = new ObjectMapper();

    /** sessionId -> 最近一次模型调用的完整上下文 */
    private final ConcurrentHashMap<Integer, CallContext> contexts = new ConcurrentHashMap<>();

    @Override
    public Flux<AgentEvent> onModelCall(Agent agent, RuntimeContext runtimeContext, ModelCallInput input,
                                        Function<ModelCallInput, Flux<AgentEvent>> next) {
        Integer sessionId = resolveSessionId(runtimeContext);
        if (sessionId != null) {
            try {
                Map<String, Object> data = new LinkedHashMap<>();
                data.put("messages", input.messages());
                data.put("tools", input.tools().stream()
                        .map(ToolSchema::getName)
                        .toList());
                contexts.put(sessionId, new CallContext(objectMapper.writeValueAsString(data), null));
            } catch (Exception e) {
                log.warn("Failed to capture model call context", e);
            }
        }

        return next.apply(input).doOnNext(event -> {
            if (event instanceof ModelCallEndEvent end && sessionId != null) {
                CallContext context = contexts.get(sessionId);
                if (context != null) {
                    try {
                        context.usageJson = objectMapper.writeValueAsString(end.getUsage());
                    } catch (Exception e) {
                        log.warn("Failed to capture usage", e);
                    }
                }
            }
        });
    }

    public CallContext get(Integer sessionId) {
        return contexts.get(sessionId);
    }

    /**
     * 记录一轮完整对话的 AI 整体输出（含 <Thinking>/<FunctionCall>/<TokenUsage> 等原始标签），
     * 供调试接口 /context 返回临时上下文时展示输出。
     */
    public void setOutput(Integer sessionId, String output) {
        CallContext context = contexts.get(sessionId);
        if (context != null) {
            context.output = output;
        }
    }

    private Integer resolveSessionId(RuntimeContext runtimeContext) {
        if (runtimeContext == null) {
            return null;
        }
        ChatContext chatContext = runtimeContext.get(ChatContext.class);
        return chatContext != null ? chatContext.getSessionId() : null;
    }

    /** 一次模型调用的完整上下文（含整轮 AI 输出） */
    public static class CallContext {
        public final String inputJson;
        public volatile String usageJson;
        /** 整轮对话的 AI 整体输出，由 ChatOrchestrator 在轮次结束时写入 */
        public volatile String output;

        CallContext(String inputJson, String usageJson) {
            this.inputJson = inputJson;
            this.usageJson = usageJson;
        }
    }
}
