package io.github.mangomaner.mangobot.adapter.onebot.service;

import io.github.mangomaner.mangobot.adapter.onebot.handler.outbound.build_sending_message.OneBotMessageBuilder;
import io.github.mangomaner.mangobot.adapter.onebot.handler.outbound.build_sending_message.OneBotSendingMessage;
import io.github.mangomaner.mangobot.adapter.onebot.handler.outbound.send.OneBotApiService;
import io.github.mangomaner.mangobot.adapter.onebot.model.vo.FriendInfo;
import io.github.mangomaner.mangobot.adapter.onebot.model.vo.GroupInfo;
import io.github.mangomaner.mangobot.adapter.onebot.model.vo.LoginInfo;
import io.github.mangomaner.mangobot.adapter.onebot.model.vo.MessageId;
import io.github.mangomaner.mangobot.adapter.service.ChatApiService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * OneBot 平台聊天 API 实现
 */
@Service
@RequiredArgsConstructor
public class OneBotChatApiService implements ChatApiService {

    private static final String PLATFORM_TYPE = "onebot_qq";

    private final OneBotApiService oneBotApiService;

    @Override
    public String getPlatformType() {
        return PLATFORM_TYPE;
    }

    @Override
    public List<GroupInfo> getGroupList(long botId) {
        return oneBotApiService.getGroupList(botId);
    }

    @Override
    public List<FriendInfo> getFriendList(long botId) {
        return oneBotApiService.getFriendList(botId);
    }

    @Override
    public MessageId sendGroupMsg(long botId, long groupId, String message) {
        OneBotSendingMessage sendMessage = OneBotMessageBuilder.create()
                .text(message)
                .build();
        return oneBotApiService.sendGroupMsg(botId, groupId, sendMessage);
    }

    @Override
    public MessageId sendPrivateMsg(long botId, long userId, String message) {
        OneBotSendingMessage sendMessage = OneBotMessageBuilder.create()
                .text(message)
                .build();
        return oneBotApiService.sendPrivateMsg(botId, userId, sendMessage);
    }

    @Override
    public LoginInfo getLoginInfo(long botId) {
        return oneBotApiService.getLoginInfo(botId);
    }
}
