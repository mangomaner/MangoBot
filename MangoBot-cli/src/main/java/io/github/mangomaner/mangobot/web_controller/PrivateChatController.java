package io.github.mangomaner.mangobot.web_controller;

import io.github.mangomaner.mangobot.adapter.onebot.model.vo.FriendInfo;
import io.github.mangomaner.mangobot.adapter.onebot.model.vo.MessageId;
import io.github.mangomaner.mangobot.adapter.service.ChatApiService;
import io.github.mangomaner.mangobot.system.common.BaseResponse;
import io.github.mangomaner.mangobot.system.common.ResultUtils;
import io.github.mangomaner.mangobot.system.common.ErrorCode;
import io.github.mangomaner.mangobot.system.exception.BusinessException;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Slf4j
@RestController
@RequestMapping("/api/chat/private")
@Tag(name = "私聊接口", description = "私聊消息发送和好友列表管理")
public class PrivateChatController {

    private final Map<String, ChatApiService> chatApiServices;

    public PrivateChatController(List<ChatApiService> services) {
        this.chatApiServices = services.stream()
                .collect(Collectors.toMap(ChatApiService::getPlatformType, Function.identity()));
    }

    @GetMapping("/friends")
    @Operation(summary = "获取好友列表")
    public BaseResponse<List<FriendInfo>> getFriendList(
            @RequestParam long botId,
            @RequestParam String platform) {
        log.info("获取好友列表: botId={}, platform={}", botId, platform);
        ChatApiService service = getService(platform);
        @SuppressWarnings("unchecked")
        List<FriendInfo> result = (List<FriendInfo>) service.getFriendList(botId);
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
        ChatApiService service = getService(platform);
        @SuppressWarnings("unchecked")
        MessageId result = (MessageId) service.sendPrivateMsg(botId, userId, message);
        return ResultUtils.success(result);
    }

    private ChatApiService getService(String platform) {
        ChatApiService service = chatApiServices.get(platform);
        if (service == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "不支持的平台: " + platform);
        }
        return service;
    }
}
