package org.mango.mangobot.messageHandler;

import dev.langchain4j.community.model.dashscope.QwenChatModel;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.mango.mangobot.annotation.QQ.method.AtMessage;
import org.mango.mangobot.annotation.QQ.method.ImageMessage;
import org.mango.mangobot.annotation.QQ.method.PokeMessage;
import org.mango.mangobot.annotation.QQ.method.TextMessage;
import org.mango.mangobot.annotation.QQ.parameter.*;
import org.mango.mangobot.service.impl.GroupMessageService;
import org.springframework.stereotype.Component;

/**
 *
 * 相关注解代码在 org/mangobot/annotation/QQ 定义，请勿混淆 方法注解 和 参数注解
 *  规则：
 *      1. 一次只会有一个方法被调用
 *      2. 精准匹配，收到消息的类型需要和你注解标注的类型 完全相同
 *      3. 按需取用 参数注解 ，参数名称可以随意取，只要标注对应注解即可
 *  建议：
 *      1. 请勿将 实际不会出现的消息组合 注解到同一个方法上，这会导致该方法永远不会执行
 */
@Slf4j
@Component
public class GroupMessageHandler {

    @Resource
    GroupMessageService groupMessageService;
    @Resource
    private QwenChatModel qwenChatModel;


    @TextMessage
    public void handleText(@Content String sss,
                           @GroupId String groupId) {
        log.info("收到文本消息: {}, {}", sss, groupId);
    }

    @ImageMessage
    public void handleImage(@ImageURL String imageUrl) {
        log.info("收到图片消息: {}", imageUrl);
    }

    @AtMessage(self = true)
    @TextMessage
    @ImageMessage
    public void handleAtBot(@SenderId String fromUser,
                            @Content String content,
                            @GroupId String groupId) {
        log.info("收到 @ 我的消息，内容为: {}, {}, {}", content, fromUser, groupId);
    }
    @AtMessage(self = false)
    @TextMessage
    public void handleAtOther(@SenderId String fromUser,
                              @Content String content,
                              @GroupId String groupId,
                              @TargetId String targetUser) {
        log.info("收到 @ {}的消息，内容为: {}, {}, {}", targetUser, content, fromUser, groupId);
    }
    /**
     *  当消息包含了 文本、at自己和图片 消息时，会调用此方法。(若希望@他人也可以触发再加一个self=false)
     */
    @AtMessage(self = true)
    @TextMessage
    public void handleAtBotWithImage(@SenderId String fromUser,
                            @Content String content,
                            @GroupId String groupId,
                            @ImageURL String imageUrl) {
        log.info("收到 @ 我的消息，内容为: {}, {}, {}", content, fromUser, groupId);
    }

    /**
     * 戳一戳事件（单独事件，请勿和其他进行组合）
     * @param fromUser
     * @param targetUser
     */
    @PokeMessage
    public void handlePoke(@SenderId String fromUser,
                           @TargetId String targetUser) {
        log.info("收到 @ {}的戳一戳，来自：{}", targetUser, fromUser);
    }
}