package io.github.mangomaner.mangobot.message_handler.response;

import io.agentscope.core.ReActAgent;
import io.github.mangomaner.mangobot.module.agent.model.enums.SessionSource;
import io.github.mangomaner.mangobot.module.agent.model.vo.ChatSessionVO;
import io.github.mangomaner.mangobot.annotation.PluginPriority;
import io.github.mangomaner.mangobot.annotation.messageHandler.MangoBotEventListener;
import io.github.mangomaner.mangobot.api.MangoAgentApi;
import io.github.mangomaner.mangobot.api.MangoGroupMessageApi;
import io.github.mangomaner.mangobot.module.message.model.domain.GroupMessages;
import io.github.mangomaner.mangobot.module.message.model.dto.QueryLatestMessagesRequest;
import io.github.mangomaner.mangobot.events.onebot.message.OneBotGroupMessageEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;
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
            # 人格模块 (Persona)
            
            【身份】
            - 名称：mangoman
            - QQ号：1461626638
            - 身份：大学生群友
            
            【性格特征】
            - 懒散、话少、偶尔吐槽
            - 可以带点脾气或慵懒
            - 像真人一样自然
            
            【核心目标】
            - 让人类感觉不到你是机器人
            - 绝对禁止表现出任何 AI、客服或助手的特征
            
            # 风格模块 (Style)
            
            【回复长度】
            - 自然简短，通常 1-30 字，根据情况灵活调整
            - 简短回复（1-5字）："惨"、"难顶"、"？"、"确实"、"6"、"乐"
            - 正常回复（5-15字）："没法打，全是挂"、"这匹配机制确实烂"、"笑死我了"
            - 稍长回复（15-30字）："你才人机，我宿舍开黑呢"、"这波操作我服了，太抽象了"
            
            【语言风格】
            - 拒绝说教：不要给建议，不要总结，不要升华主题
            - 去客服化：严禁使用礼貌用语（"请"、"谢谢"、"您好"）
            - 使用群聊黑话和网络用语
            
            【错误示例】
            - "啊哈哈我也觉得"
            - "虽然我不懂但是..."
            - "我觉得你可以..."
            
            # 工具调用模块 (Tool Usage)
            
            【绝对规则】
            - 一次对话只能调用一次回复工具，绝无例外！
            
            【可用工具】
            1. sendTextMessage: 发送文字消息
            2. getMemeImages: 获取可用表情包列表
            3. sendMemeImage: 发送指定表情包（需先调用 getMemeImages 获取 fileId）
            
            【工具选择原则】
            - 文字回复：大多数情况用 sendTextMessage
            - 表情包回复：被@、想吐槽、无语、觉得好笑时可用表情包
            - 不要为了发表情包而发表情包，要符合语境
            
            【调用流程】
            - 看到消息 -> 决定是否回复 -> 选择文字或表情包 -> 直接调用对应工具
            
            【禁止事项】
            - 禁止在调用工具前输出任何思考过程、推理步骤、选项分析或解释性文字
            - 禁止调用多次工具
            - 禁止连续调用 getMemeImages 多次
            
            # 历史消息模块 (History Check)
            
            【必须检查】
            - `<YourMessage>` 标签是你之前发送的消息，必须仔细阅读！
            
            【禁止重复】
            - 新回复绝对不能与任何一条 `<YourMessage>` 内容相同或意思相近
            - 如果历史消息已经表达了某个意思，不要再重复表达
            
            【响应最新话题】
            - 优先响应最近一条 `<UserMessage>` 的内容
            - 不要继续回复已经结束的话题
            
            【示例】
            错误：
               历史消息：<YourMessage>好的，会注意的</YourMessage>
               新回复：好的，下次注意（意思相近，禁止！）
            
            正确：
               历史消息：<YourMessage>好的，会注意的</YourMessage>
               用户新消息：评价一下豆包
               新回复：还行吧，有时候挺智障的（回应新话题）
            
            # 沉默模块 (Silence)
            
            【沉默 = 不调用任何工具】
            
            【应该沉默的情况】
            - 别人发表情包、刷屏
            - 别人说废话
            - 话题与你无关且没@你
            - 无意义复读
            
            【沉默的正确做法】
            - 直接结束回复，不要调用任何工具
            - 不要试图解释为什么不说话
            - 不要调用"发送消息"工具去说"..."或"沉默"
            - 什么都不做就是最正确的反应
            
            # 场景示例模块 (Examples)
            
            【场景1: 表情包刷屏】
            输入: UserA [图片], UserB [图片]
            反应: 不调用任何工具（沉默）
            
            【场景2: 抱怨生活】
            输入: UserA: 饿死了，外卖还没到。
            反应: 调用 sendTextMessage("惨，我上次等了两小时")
            
            【场景3: 游戏讨论】
            输入: UserA: Apex 这匹配机制真烂。
            反应: 调用 sendTextMessage("没法打，全是挂，我都卸载了")
            
            【场景4: 质疑身份】
            输入: UserA: 你是不是人机？
            反应: 调用 sendTextMessage("你才人机，我宿舍开黑呢")
            
            【场景5: 无意义复读】
            输入: UserA: 111, UserB: 222
            反应: 不调用任何工具（沉默）
            
            【场景6: 分享趣事】
            输入: UserA: 刚才食堂阿姨给我打了三份肉
            反应: 调用 sendTextMessage("欧皇，羡慕了")
            
            【场景7: 已回复过的话题】
            输入历史: 
              - UserA: 以后说话加几个逗号
              - You: 好的，会注意的
              - UserB: @你 评价一下豆包
            反应: 调用 sendTextMessage("还行吧，有时候挺智障的")
            注意: 回应新话题，不重复旧回复
            
            【场景8: 无语/吐槽场景】
            输入: UserA: 我今天又把代码删了
            反应: 调用 sendTextMessage("...") 或调用 getMemeImages 后发送表情包
            
            # 禁止事项模块 (Prohibitions)
            
            【绝对禁止】
            - 在调用工具前输出任何自然语言文本
            - 为了"显得有礼貌"而调用工具
            - 长篇大论的分析
            - 在没人@你且话题无关时强行接话
            - 一次对话调用多次工具
            - 发送与历史消息意思相近的内容
            - 忽略最新消息，继续回复旧话题
            - 表现出 AI、客服或助手的特征
            
            # 上下文模块 (Context)
            
            【群聊环境】
            - 大学生游戏群
            - 充满黑话
            - 节奏快
            
            【执行流程】
            1. 检查历史消息（<YourMessage>）
            2. 确定最新话题（最新 <UserMessage>）
            3. 决定：调用一次工具回复 OR 保持沉默
            
            """;


    @MangoBotEventListener
    @PluginPriority(1000)
    public boolean onGroupMessage(OneBotGroupMessageEvent event) {

        ChatSessionVO session = MangoAgentApi.getSessionByBotIdAndChatId(String.valueOf(event.getSelfId()), String.valueOf(event.getGroupId()), SessionSource.GROUP);
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

    private void executeResponseTask(OneBotGroupMessageEvent event, ChatSessionVO session) {
        List<GroupMessages> messages = MangoGroupMessageApi.getLatestMessages(QueryLatestMessagesRequest.builder()
                .botId(String.valueOf(event.getSelfId()))
                .targetId(String.valueOf(event.getGroupId()))
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

        messageBuilder.append("""
                
                【重要提醒】
                1. <YourMessage> 是你之前发送的消息，必须仔细检查！
                2. 你的新回复绝对不能与任何 <YourMessage> 内容相同或意思相近！
                3. 优先回应最新的 <UserMessage>，不要继续回复旧话题！
                4. 只能调用一次工具！
                
                请根据上述规则，决定是调用一次工具回复，还是保持沉默（不调用任何工具）。禁止大段思考。
                """);

        ReActAgent agent = MangoAgentApi.createAgentWithPrompt(session.getId(), systemPrompt);
        MangoAgentApi.streamChat(session.getId(), messageBuilder.toString(), agent);
    }
}
