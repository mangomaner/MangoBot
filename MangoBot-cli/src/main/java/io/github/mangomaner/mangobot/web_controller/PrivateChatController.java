package io.github.mangomaner.mangobot.web_controller;

import io.github.mangomaner.mangobot.adapter.onebot.model.vo.FriendInfo;
import io.github.mangomaner.mangobot.adapter.onebot.model.vo.MessageId;
import io.github.mangomaner.mangobot.adapter.onebot.handler.outbound.build_sending_message.OneBotMessageBuilder;
import io.github.mangomaner.mangobot.adapter.onebot.handler.outbound.build_sending_message.OneBotSendingMessage;
import io.github.mangomaner.mangobot.adapter.onebot.handler.outbound.send.OneBotApiService;
import io.github.mangomaner.mangobot.system.common.BaseResponse;
import io.github.mangomaner.mangobot.system.common.ResultUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/chat/private")
@RequiredArgsConstructor
@Tag(name = "私聊接口", description = "私聊消息发送和好友列表管理")
public class PrivateChatController {

    private final OneBotApiService oneBotApiService;

    @GetMapping("/friends")
    @Operation(summary = "获取好友列表")
    public BaseResponse<List<FriendInfo>> getFriendList(
            @RequestParam long botId,
            @RequestParam String platform) {
        log.info("获取好友列表: botId={}, platform={}", botId, platform);
        List<FriendInfo> result = oneBotApiService.getFriendList(botId);
        return ResultUtils.success(result);
    }

    @PostMapping("/send")
    @Operation(summary = "发送私聊消息")
    public BaseResponse<MessageId> sendPrivateMsg(
            @RequestParam long botId,
            @RequestParam long userId,
            @RequestParam String message,
            @RequestParam String platform) {
        log.info("发送私聊消息: botId={}, userId={}, message={}, platform={}", botId, userId, message, platform);
        OneBotSendingMessage sendMessage = OneBotMessageBuilder.create()
                .text(message)
                .build();
        MessageId result = oneBotApiService.sendPrivateMsg(botId, userId, sendMessage);
        return ResultUtils.success(result);
    }
}
