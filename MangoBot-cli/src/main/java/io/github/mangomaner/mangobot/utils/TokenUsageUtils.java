package io.github.mangomaner.mangobot.utils;

import io.agentscope.core.hook.PostCallEvent;
import io.agentscope.core.message.Msg;
import io.agentscope.core.model.ChatUsage;
import io.github.mangomaner.mangobot.agent.model.vo.TokenUsageVO;
import lombok.extern.slf4j.Slf4j;

/**
 * Token 用量统计工具类
 * <p>
 * 提供从 AgentScope 的 Msg 或 PostCallEvent 中提取 Token 用量信息的能力。
 * 直接使用 AgentScope 提供的 ChatUsage 类获取数据。
 */
@Slf4j
public final class TokenUsageUtils {

    private TokenUsageUtils() {
        // 私有构造函数，防止实例化
    }

    /**
     * 从 PostCallEvent 中提取 Token 用量信息
     *
     * @param event PostCallEvent 事件
     * @return TokenUsageVO 或 null（如果提取失败或无用量信息）
     */
    public static TokenUsageVO extractFromEvent(PostCallEvent event) {
        if (event == null) {
            log.debug("[TokenUsage] PostCallEvent 为空");
            return null;
        }

        Msg finalMessage = event.getFinalMessage();
        return extractFromMsg(finalMessage);
    }

    /**
     * 从 Msg 消息中提取 Token 用量信息
     *
     * @param msg Msg 消息对象
     * @return TokenUsageVO 或 null（如果提取失败或无用量信息）
     */
    public static TokenUsageVO extractFromMsg(Msg msg) {
        if (msg == null) {
            log.debug("[TokenUsage] Msg 为空");
            return null;
        }

        ChatUsage chatUsage = msg.getChatUsage();
        if (chatUsage == null) {
            log.debug("[TokenUsage] ChatUsage 为空，模型可能不支持用量统计");
            return null;
        }

        return extractFromChatUsage(chatUsage);
    }

    /**
     * 从 ChatUsage 对象中提取 Token 用量信息
     * <p>
     * 直接使用 ChatUsage 类的 getter 方法获取数据
     *
     * @param chatUsage ChatUsage 对象
     * @return TokenUsageVO 或 null（如果提取失败）
     */
    public static TokenUsageVO extractFromChatUsage(ChatUsage chatUsage) {
        if (chatUsage == null) {
            return null;
        }

        try {
            TokenUsageVO result = TokenUsageVO.builder()
                    .inputTokens((long) chatUsage.getInputTokens())
                    .outputTokens((long) chatUsage.getOutputTokens())
                    .totalTokens((long) chatUsage.getTotalTokens())
                    .time(chatUsage.getTime())
                    .build();

            log.debug("[TokenUsage] 提取成功: input={}, output={}, time={}s",
                    result.getInputTokens(), result.getOutputTokens(), result.getTime());
            return result;
        } catch (Exception e) {
            log.error("[TokenUsage] 获取 ChatUsage 属性失败", e);
            return null;
        }
    }
}
