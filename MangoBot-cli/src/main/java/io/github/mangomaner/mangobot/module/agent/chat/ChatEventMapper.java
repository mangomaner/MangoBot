package io.github.mangomaner.mangobot.module.agent.chat;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.agentscope.core.event.AgentEvent;
import io.agentscope.core.event.TextBlockDeltaEvent;
import io.agentscope.core.event.ThinkingBlockDeltaEvent;
import io.agentscope.core.event.ThinkingBlockEndEvent;
import io.agentscope.core.event.ThinkingBlockStartEvent;
import io.agentscope.core.event.ToolCallStartEvent;
import io.agentscope.core.event.ToolResultEndEvent;
import io.agentscope.core.event.ToolResultTextDeltaEvent;
import io.github.mangomaner.mangobot.module.agent.model.vo.TokenUsageVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.concurrent.ConcurrentHashMap;

/**
 * 把 AgentScope v2 的类型化事件流映射回前端/IM 的 SSE 文本协议。
 *
 * <p>输出格式：
 * <ul>
 *   <li>正文/思考增量：原文（换行转 \u0000），思考块用 &lt;Thinking&gt;...&lt;/Thinking&gt; 包裹；</li>
 *   <li>工具调用：&lt;FunctionCall&gt;name&lt;/FunctionCall&gt;&lt;FunctionCallResult&gt;text&lt;/FunctionCallResult&gt;
 *       在工具执行完成时一次性输出（保证相邻、不错位，空结果不输出 result 标签）；</li>
 *   <li>Token 用量：&lt;TokenUsage&gt;{json}&lt;/TokenUsage&gt;。</li>
 * </ul>
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ChatEventMapper {

    private final ObjectMapper objectMapper;

    /** replyId:toolCallId -> 工具结果增量缓冲 */
    private final ConcurrentHashMap<String, StringBuilder> toolResultBuffers = new ConcurrentHashMap<>();

    /**
     * 将单个事件映射为 SSE 文本；无输出的事件返回 null。
     */
    public String map(AgentEvent event) {
        if (event instanceof ThinkingBlockStartEvent) {
            return "<Thinking>";
        }
        if (event instanceof ThinkingBlockDeltaEvent delta) {
            return escape(delta.getDelta());
        }
        if (event instanceof ThinkingBlockEndEvent) {
            return "</Thinking>";
        }
        if (event instanceof TextBlockDeltaEvent delta) {
            return escape(delta.getDelta());
        }
        if (event instanceof ToolCallStartEvent start) {
            // 调用开始立即输出名称（带 id），保证工具调用可见；
            // 结果完成后由 ToolResultEndEvent 按同一 id 输出，前端按 id 配对，不受思考/正文交错影响
            String id = safeId(start.getToolCallId());
            String idAttr = id != null ? " id=\"" + id + "\"" : "";
            return "<FunctionCall" + idAttr + ">" + start.getToolCallName() + "</FunctionCall>\n";
        }
        if (event instanceof ToolResultTextDeltaEvent delta) {
            toolResultBuffers
                    .computeIfAbsent(key(delta.getReplyId(), delta.getToolCallId()), k -> new StringBuilder())
                    .append(delta.getDelta());
            return null;
        }
        if (event instanceof ToolResultEndEvent end) {
            String callKey = key(end.getReplyId(), end.getToolCallId());
            StringBuilder buffer = toolResultBuffers.remove(callKey);
            String result = buffer != null ? buffer.toString() : "";
            if (result.isBlank()) {
                return null;
            }
            String id = safeId(end.getToolCallId());
            String idAttr = id != null ? " id=\"" + id + "\"" : "";
            return "<FunctionCallResult" + idAttr + ">" + result + "</FunctionCallResult>\n";
        }
        // ModelCallEndEvent 的用量由 ChatOrchestrator 汇总，整轮结束时统一输出一次（避免一轮多次模型调用产生多条）
        return null;
    }

    public String serializeTokenUsage(TokenUsageVO vo) {
        try {
            return "<TokenUsage>" + objectMapper.writeValueAsString(vo) + "</TokenUsage>\n";
        } catch (Exception e) {
            log.error("[TokenUsage] JSON 序列化失败", e);
            return null;
        }
    }

    private static String key(String replyId, String toolCallId) {
        return replyId + ":" + toolCallId;
    }

    private static String safeId(String toolCallId) {
        if (toolCallId == null || toolCallId.isBlank()) {
            return null;
        }
        return toolCallId.replaceAll("[^A-Za-z0-9_-]", "_");
    }

    private static String escape(String text) {
        if (text == null || text.isEmpty()) {
            return "";
        }
        return text.replace("\n", "\u0000");
    }
}
