package io.github.mangomaner.mangobot.web_controller;

import io.github.mangomaner.mangobot.adapter.onebot.model.vo.FriendInfo;
import io.github.mangomaner.mangobot.adapter.onebot.model.vo.GroupInfo;
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
@RequestMapping("/api/chat/group")
@Tag(name = "群聊接口", description = "群聊消息发送和群列表管理")
public class GroupChatController {

    private final Map<String, ChatApiService> chatApiServices;

    public GroupChatController(List<ChatApiService> services) {
        this.chatApiServices = services.stream()
                .collect(Collectors.toMap(ChatApiService::getPlatformType, Function.identity()));
    }

    @GetMapping("/list")
    @Operation(summary = "获取群列表")
    public BaseResponse<List<GroupInfo>> getGroupList(
            @RequestParam long botId,
            @RequestParam String platform) {
        log.info("获取群列表: botId={}, platform={}", botId, platform);
        ChatApiService service = getService(platform);
        @SuppressWarnings("unchecked")
        List<GroupInfo> result = (List<GroupInfo>) service.getGroupList(botId);
        return ResultUtils.success(result);
    }

    @PostMapping("/send")
    @Operation(summary = "发送群消息")
    public BaseResponse<MessageId> sendGroupMsg(
            @RequestParam long botId,
            @RequestParam long groupId,
            @RequestParam String message,
            @RequestParam String platform) {
        log.info("发送群消息: botId={}, groupId={}, message={}, platform={}", botId, groupId, message, platform);
        ChatApiService service = getService(platform);
        @SuppressWarnings("unchecked")
        MessageId result = (MessageId) service.sendGroupMsg(botId, groupId, message);
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
