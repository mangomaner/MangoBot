package io.github.mangomaner.mangobot.web_controller;

import io.github.mangomaner.mangobot.adapter.onebot.model.vo.GroupInfo;
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
@RequestMapping("/api/chat/group")
@RequiredArgsConstructor
@Tag(name = "群聊接口", description = "群聊消息发送和群列表管理")
public class GroupChatController {

    private final OneBotApiService oneBotApiService;

    @GetMapping("/list")
    @Operation(summary = "获取群列表")
    public BaseResponse<List<GroupInfo>> getGroupList(
            @RequestParam long botId,
            @RequestParam String platform) {
        log.info("获取群列表: botId={}, platform={}", botId, platform);
        List<GroupInfo> result = oneBotApiService.getGroupList(botId);
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
        OneBotSendingMessage sendMessage = OneBotMessageBuilder.create()
                .text(message)
                .build();
        MessageId result = oneBotApiService.sendGroupMsg(botId, groupId, sendMessage);
        return ResultUtils.success(result);
    }
}
