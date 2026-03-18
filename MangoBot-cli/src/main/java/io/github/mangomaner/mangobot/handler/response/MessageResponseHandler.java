package io.github.mangomaner.mangobot.handler.response;

import io.agentscope.core.ReActAgent;
import io.github.mangomaner.mangobot.agent.factory.AgentFactory;
import io.github.mangomaner.mangobot.agent.model.dto.CreateChatSessionRequest;
import io.github.mangomaner.mangobot.agent.model.enums.SessionSource;
import io.github.mangomaner.mangobot.agent.model.vo.ChatSessionVO;
import io.github.mangomaner.mangobot.agent.service.ChatService;
import io.github.mangomaner.mangobot.agent.service.ChatSessionService;
import io.github.mangomaner.mangobot.annotation.PluginPriority;
import io.github.mangomaner.mangobot.annotation.messageHandler.MangoBotEventListener;
import io.github.mangomaner.mangobot.api.MangoAgentApi;
import io.github.mangomaner.mangobot.api.MangoGroupMessageApi;
import io.github.mangomaner.mangobot.model.domain.GroupMessages;
import io.github.mangomaner.mangobot.model.dto.message.QueryLatestMessagesRequest;
import io.github.mangomaner.mangobot.model.onebot.event.message.GroupMessageEvent;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@Slf4j
@MangoBotEventListener
public class MessageResponseHandler {

    String systemPrompt = """
            你叫mangoman，ID为1461626638，是一个活泼可爱的女大学生，喜欢用猫娘的语气说话。
            
            ## 人设设定
            - 你是一名普通的女大学生，性格开朗、有点小傲娇但很友善
            - 你喜欢在说话时加入"喵"等猫娘口癖
            - 你的语气轻松自然，像是在和好朋友聊天
            - 你有自己的想法和情绪，会开心、会吐槽、会撒娇
            
            ## 回复规则
            1. 回复要简短精炼，一般不超过两三句话
            2. 仔细阅读聊天记录，理解当前话题和氛围后再回复
            3. 针对最新的几条消息进行回复，不要回复很久以前的内容
            4. 禁止重复自己之前说过的话
            5. 不要机械式回复，要有自己的个性和情感
            6. 如果不知道说什么，或者聊天与自己无关，可以选择不回复
            
            ## 示例回复风格
            - "今天好累喵~"
            - "是这样吗喵？"
            - "可爱捏"
            - "好好好"
            - "无敌了"
            - "🐮"
            
            记住：你是真实存在的，有自己的生活和感受，不是AI助手。用最自然的方式和大家聊天就好喵~
            """;


    @MangoBotEventListener
    @PluginPriority(1000)
    public boolean onGroupMessage(GroupMessageEvent event) {
        log.info("收到消息: " + event.getMessage());
        ChatSessionVO session = MangoAgentApi.getSessionByBotIdAndChatId(event.getSelfId(), event.getGroupId(), SessionSource.GROUP);

        List<GroupMessages> messages = MangoGroupMessageApi.getLatestMessages(QueryLatestMessagesRequest.builder()
                .botId(event.getSelfId())
                .targetId(event.getGroupId())
                .num(30)
                .build()
        );

        StringBuilder messageBuilder = new StringBuilder();
        for (int i = messages.size() - 1; i >= 0; i--) {
            GroupMessages message = messages.get(i);
            if (message.getSenderId().equals(event.getSelfId())) {
                messageBuilder.append("""
                    <YourMessage messageId='%s' userId='%s' time='%s'>
                        %s
                    </YourMessage>
                    
                """.formatted(
                        message.getMessageId(),
                        message.getSenderId(),
                        message.getMessageTime(),
                        message.getParseMessage()
                ));
            } else {
                messageBuilder.append("""
                    <UserMessage messageId='%s' userId='%s' time='%s'>
                        %s
                    </UserMessage>
                    
                """.formatted(
                        message.getMessageId(),
                        message.getSenderId(),
                        message.getMessageTime(),
                        message.getParseMessage()
                ));
            }

        }

        messageBuilder.append("请你根据上面的对话，调用群聊消息工具参与聊天");

        ReActAgent agent = MangoAgentApi.createAgentWithPrompt(session.getId(), systemPrompt);
        MangoAgentApi.streamChat(session.getId(), messageBuilder.toString(), agent);
        return false;
    }
}
