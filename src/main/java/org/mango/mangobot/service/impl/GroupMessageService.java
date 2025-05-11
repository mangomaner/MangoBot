package org.mango.mangobot.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.mango.mangobot.common.ErrorCode;
import org.mango.mangobot.exception.BusinessException;
import org.mango.mangobot.manager.websocketReverseProxy.model.dto.Message;
import org.mango.mangobot.manager.websocketReverseProxy.model.dto.groupMessage.*;
import org.mango.mangobot.service.GroupMessage;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
@Slf4j
public class GroupMessageService implements GroupMessage {

    private final ObjectMapper objectMapper = new ObjectMapper();
    @Resource
    private Map<String, WebSocketSession> sessionMap; // 假设 SessionMap 是你管理 WebSocketSession 的组件

    @Value("${QQ.botQQ}")
    private String selfId;

    /**
     * 发送纯文本消息
     */
    @Override
    public void sendTextMessage(String groupId, String text) {
        SendGroupMessageRequest request = new SendGroupMessageRequest();
        request.setGroup_id(groupId);
        request.setMessage(List.of(new TextMessageData() {{
            getData().setText(text);
        }}));

        sendMessage(selfId, "send_group_msg", request);
    }

    /**
     * 发送带 @ 的消息
     */
    @Override
    public void sendAtMessage(String groupId, String qq, String text) {
        SendGroupMessageRequest request = new SendGroupMessageRequest();
        request.setGroup_id(groupId);
        request.setMessage(List.of(
                new AtMessageData() {{
                    getData().setQq(qq);
                }},
                new TextMessageData() {{
                    getData().setText(" " + text);
                }}
        ));

        sendMessage(selfId, "send_group_msg", request);
    }

    /**
     * 发送图片消息
     */
    @Override
    public void sendImageMessage(String groupId, String fileUrlOrPath) {
        SendGroupMessageRequest request = new SendGroupMessageRequest();
        request.setGroup_id(groupId);
        request.setMessage(List.of(new ImageMessageData() {{
            getData().setFile(fileUrlOrPath);
        }}));

        sendMessage(selfId, "send_group_msg", request);
    }

    /**
     * 发送语音消息
     */
    @Override
    public void sendRecordMessage(String groupId, String fileUrlOrPath) {
        SendGroupMessageRequest request = new SendGroupMessageRequest();
        request.setGroup_id(groupId);
        request.setMessage(List.of(new RecordMessageData() {{
            getData().setFile(fileUrlOrPath);
        }}));

        sendMessage(selfId, "send_group_msg", request);
    }

    /**
     * 发送回复消息
     */
    @Override
    public void sendReplyMessage(String groupId, String messageId, String message) {
        SendGroupMessageRequest request = new SendGroupMessageRequest();
        request.setGroup_id(groupId);
        request.setMessage(List.of(
                new ReplyMessageData() {{
                    getData().setId(messageId);
                }},
                new TextMessageData() {{
                    getData().setText(message);
                }}
        ));

        sendMessage(selfId, "send_group_msg", request);
    }

    /**
     * 发送混合消息（可自定义多个 MessageSegment）
     */
    @Override
    public void sendCustomMessage(String groupId, String text, String qq, String imageUrl) {
        if(text == null && qq == null && imageUrl == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "当前发送的是空消息");
        }

        SendGroupMessageRequest request = new SendGroupMessageRequest();
        List<MessageSegment> segments = new ArrayList<>();

        if (qq != null && !qq.isEmpty()) {
            segments.add(new AtMessageData() {{
                getData().setQq(qq);
            }});
        }

        if (text != null && !text.isEmpty()) {
            segments.add(new TextMessageData() {{
                getData().setText(" " + text);
            }});
        }

        if (imageUrl != null && !imageUrl.isEmpty()) {
            segments.add(new ImageMessageData() {{
                getData().setFile(imageUrl);
            }});
        }

        request.setGroup_id(groupId);
        request.setMessage(segments);

        sendMessage(selfId, "send_group_msg", request);
    }

    /**
     * 内部方法：构造并发送 Message 对象
     */
    private <T> void sendMessage(String selfId, String action, T params) {
        try {
            WebSocketSession session = sessionMap.get(selfId);
            if (session == null || !session.isOpen()) {
                log.warn("QQ号 {} 当前没有活跃连接", selfId);
                return;
            }

            Message<T> messageWrapper = new Message<>();
            messageWrapper.setAction(action);
            messageWrapper.setParams(params);

            String json = objectMapper.writeValueAsString(messageWrapper);
            session.sendMessage(new TextMessage(json));
            log.info("消息已发送: {}", json);
        } catch (Exception e) {
            log.error("发送消息时出错: ", e);
        }
    }
}