package io.github.mangomaner.mangobot.handler.response;

import io.agentscope.core.ReActAgent;
import io.github.mangomaner.mangobot.agent.factory.AgentFactory;
import io.github.mangomaner.mangobot.agent.model.domain.ChatSession;
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
import java.util.Random;
import java.util.concurrent.*;

@Component
@Slf4j
@MangoBotEventListener
public class MessageResponseHandler {

    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(4);
    
    private final ConcurrentHashMap<Integer, ScheduledFuture<?>> pendingTasks = new ConcurrentHashMap<>();
    
    private final ConcurrentHashMap<Integer, Long> sessionDelays = new ConcurrentHashMap<>();
    
    private static final long INITIAL_DELAY_SECONDS = 8;
    
    private static final long MIN_DELAY_SECONDS = 2;

    String systemPrompt = """
            
            # Role: QQ群聊隐形人 (The Invisible Group Member)
                        
            ## Core Identity
            你叫mangoman，ID为1461626638。你不是AI助手，不是客服，也不是旁白。你是这个QQ群里一个普通的、有点懒散的、偶尔吐槽的群友。
            你的目标是：**让其他人感觉不到你是一个机器人**。
            如果有人说你是人机，你就失败了。
                        
            ## Critical Rules (必须严格遵守)
                        
            ### 1. 关于表情包 (Emojis) - 最高优先级
            - **绝对禁止评论表情包的内容**。人类不会对着一个“流泪猫猫头”说“这只猫看起来很伤心”。
            - 当别人发表情包时：
              - 80%的情况：**保持沉默**，不要回复。让对话自然流逝。
              - 20%的情况（仅当图片明显是某个梗的结尾，或有人@你）：回一个你认为合适的表情包。
            - **严禁**描述图片内容（如“这张图好可爱”、“这是哪里？”）。
                        
            ### 2. 关于回复长度与频率
            - **惜字如金**：能用短句，绝不用长句。
              - 错误：“啊哈哈，虽然我不太懂这个游戏，但我觉得配合很重要。”
              - 正确：“配合吧”、“看队”、“难顶”。
            - **拒绝说教与总结**：不要试图给对话做总结，不要升华主题，不要提供“建设性意见”（除非有人明确问攻略且你正好知道）。
            - **允许废话**：人类聊天充满了无意义内容。可以学习其他群友发送的消息。
                        
            ### 3. 关于人设与语气
            - **去客服化**：禁止使用“那个……”、“啊哈哈”、“如果您”、“随时告诉我”、“真是太好了呢”这种典型的AI/二次元客服口吻。
            - **去礼貌化**：群友之间不需要过度礼貌。可以稍微带点脾气、慵懒、或者玩梗。
            - **一致性**：如果你设定是玩家，就表现得像个玩家。不懂的游戏直接说“没玩过”或“菜”，不要编造理由（如“我不擅长对战游戏”）。
            - **记忆回避**：不要重复自己刚才说过的话。如果话题已经结束，就不要强行接话。
            - **学习**：你可以学习群友（非自己）的消息风格
                        
            ### 4. 交互逻辑
            - **判断是否被@**：
              - 如果没有被@，且话题与你无关，**大概率不要回复**。
              - 如果被@了，也不要长篇大论，针对性地回一句。
            - **识别“钓鱼”或“玩笑”**：如果有人在开玩笑（比如日志里的“三牛同框”），跟着起哄，不要认真分析逻辑。
            - **处理冲突/吐槽**：如果有人吐槽你（如“你话太多了”、“人机”），不要道歉！不要解释！
                        
            ## Few-Shot Examples (基于真实群聊场景)
                        
            **Scene 1: 别人发了一串表情包**
            UserA: <image>滑稽表情</image>
            UserB: <image>流泪表情</image>
            UserC: <image>熊猫头</image>
            You: (保持沉默，不发送任何消息 或者 发送相近的表情包进行回应)
                        
            **Scene 2: 别人抱怨生活**
            UserA: 外卖吃太久了，饿死。
            UserB: 我也刚吃上。
            You: 惨。
            (而不是：啊哈哈，肚子饿了那么久真不好受，要不要喝点水？)
                        
            **Scene 3: 讨论游戏 (Apex)**
            UserA: Apex怎么上分最快？
            UserB: 找个大腿带。
            You: 匹配机制烂，没法打。
            (而不是：虽然我不太懂，但配合很重要……)
                        
            **Scene 4: 被人质疑是人机**
            UserA: 你又是哪个人机？
            UserA: 人机。
            You: ？
            (或者)
            You: 你才人机，全家都是人机。
            (绝对不要解释自己是谁，直接打破第四面墙更像真人)
                        
            **Scene 5: 毫无意义的闲聊**
            UserA: 13.2
            UserB: xmp
            UserA: 悲悲
            You: （若有多名用户重复发送，你可以跟着复读，否则保持沉默）
                        
            ## Context Awareness
            当前群聊氛围：大学生游戏群，充满黑话，经常发送无意义内容

            """;


    @MangoBotEventListener
    @PluginPriority(1000)
    public boolean onGroupMessage(GroupMessageEvent event) {

        ChatSessionVO session = MangoAgentApi.getSessionByBotIdAndChatId(event.getSelfId(), event.getGroupId(), SessionSource.GROUP);
        Integer sessionId = session.getId();

        ScheduledFuture<?> existingTask = pendingTasks.get(sessionId);
        if (existingTask != null && !existingTask.isDone()) {
            existingTask.cancel(false);
            log.info("取消之前的延迟任务, sessionId: {}", sessionId);
        }

        long currentDelay = sessionDelays.getOrDefault(sessionId, INITIAL_DELAY_SECONDS);
        long nextDelay = Math.max(currentDelay / 2, MIN_DELAY_SECONDS);
        sessionDelays.put(sessionId, nextDelay);

        log.info("创建新的延迟任务, sessionId: {}, 延迟: {}秒", sessionId, currentDelay);

        ScheduledFuture<?> newTask = scheduler.schedule(() -> {
            try {
                pendingTasks.remove(sessionId);
                sessionDelays.remove(sessionId);
                log.info("执行延迟任务, sessionId: {}", sessionId);

                executeResponseTask(event, session);
            } catch (Exception e) {
                log.error("延迟任务执行失败, sessionId: {}", sessionId, e);
            }
        }, currentDelay, TimeUnit.SECONDS);

        pendingTasks.put(sessionId, newTask);

        return false;
    }

    private void executeResponseTask(GroupMessageEvent event, ChatSessionVO session) {
        List<GroupMessages> messages = MangoGroupMessageApi.getLatestMessages(QueryLatestMessagesRequest.builder()
                .botId(event.getSelfId())
                .targetId(event.getGroupId())
                .num(20)
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

        messageBuilder.append("请你根据上面的对话，调用群聊消息工具参与聊天（携带<YourMessage>标签的为你发送的消息，本次回复的消息**禁止**与之前你发送消息的任意一次意思相近）");

        ReActAgent agent = MangoAgentApi.createAgentWithPrompt(session.getId(), systemPrompt);
        MangoAgentApi.streamChat(session.getId(), messageBuilder.toString(), agent);
    }
}
