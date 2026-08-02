package io.github.mangomaner.mangobot.message_handler.response;

import io.github.mangomaner.mangobot.annotation.PluginPriority;
import io.github.mangomaner.mangobot.annotation.messageHandler.MangoBotEventListener;
import io.github.mangomaner.mangobot.api.MangoAgentApi;
import io.github.mangomaner.mangobot.events.onebot.message.OneBotGroupMessageEvent;
import io.github.mangomaner.mangobot.events.onebot.message.OneBotPrivateMessageEvent;
import io.github.mangomaner.mangobot.module.agent.model.enums.SessionSource;
import io.github.mangomaner.mangobot.module.agent.model.vo.ChatSessionVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

/**
 * 群聊 / 私聊 AI 对话入口
 *
 * <p>v2 升级后职责极简：
 * <ul>
 *   <li>人格/行为约定在 workspace/AGENTS.md（可热改）；</li>
 *   <li>每条消息直接交给 {@link MangoAgentApi#streamChat(Integer, String, String)}，
 *       不再手工组装最近消息、不再用 XML 包装（&lt;UserMessage messageId=...&gt;），
 *       发送者身份通过 UserMessage.name 带给模型，会话历史由 AgentState 承载；</li>
 *   <li>同一会话有 in-flight 调用时，ChatOrchestrator 自动打断旧调用并基于新消息重新决策
 *       （运行中打断 + 合并续答），因此这里不需要 debounce 或延迟合并；</li>
 *   <li>私聊必须回复：ChatOrchestrator 检测到未调用发送工具时会自动重试一次。</li>
 * </ul>
 */
@Component
@Slf4j
@MangoBotEventListener
public class MessageResponseHandler {

    @MangoBotEventListener
    @PluginPriority(1000)
    public boolean onGroupMessage(OneBotGroupMessageEvent event) {
        String text = extractText(event.getParsedMessage(), event.getRawMessage());
        if (text == null) {
            return false;
        }
        ChatSessionVO session = MangoAgentApi.getSessionByBotIdAndChatId(
                String.valueOf(event.getSelfId()),
                String.valueOf(event.getGroupId()),
                SessionSource.GROUP);
        MangoAgentApi.streamChat(session.getId(), text, String.valueOf(event.getUserId())).subscribe();
        return false;
    }

    @MangoBotEventListener
    @PluginPriority(1000)
    public boolean onPrivateMessage(OneBotPrivateMessageEvent event) {
        String text = extractText(event.getParsedMessage(), event.getRawMessage());
        if (text == null) {
            return false;
        }
        ChatSessionVO session = MangoAgentApi.getSessionByBotIdAndChatId(
                String.valueOf(event.getSelfId()),
                String.valueOf(event.getUserId()),
                SessionSource.PRIVATE);
        MangoAgentApi.streamChat(session.getId(), text, String.valueOf(event.getUserId())).subscribe();
        return false;
    }

    private String extractText(String parsedMessage, String rawMessage) {
        if (StringUtils.hasText(parsedMessage)) {
            return parsedMessage;
        }
        if (StringUtils.hasText(rawMessage)) {
            return rawMessage;
        }
        return null;
    }
}
