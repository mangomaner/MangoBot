package io.github.mangomaner.mangobot.module.message.privateMessage.controller;

import io.github.mangomaner.mangobot.system.common.BaseResponse;
import io.github.mangomaner.mangobot.system.common.ResultUtils;
import io.github.mangomaner.mangobot.module.message.model.dto.QueryLatestMessagesRequest;
import io.github.mangomaner.mangobot.module.message.model.dto.QueryMessagesByMessageIdRequest;
import io.github.mangomaner.mangobot.module.message.model.dto.QueryMessagesBySenderRequest;
import io.github.mangomaner.mangobot.module.message.model.dto.SearchMessagesRequest;
import io.github.mangomaner.mangobot.module.message.model.vo.PrivateMessageVO;
import io.github.mangomaner.mangobot.module.message.privateMessage.service.PrivateMessagesService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/message/private")
@Slf4j
public class PrivateMessageController {
    @Resource
    private PrivateMessagesService privateMessagesService;
    @GetMapping("/getLatestMessages")
    public BaseResponse<List<PrivateMessageVO>> getLatestMessages(QueryLatestMessagesRequest request) {
        return ResultUtils.success(privateMessagesService.convertToVOList(privateMessagesService.getLatestMessages(request)));
    }

    @GetMapping("/more/byMessageId")
    public BaseResponse<List<PrivateMessageVO>> getMessagesByMessageId(QueryMessagesByMessageIdRequest request) {
        return ResultUtils.success(privateMessagesService.convertToVOList(privateMessagesService.getMessagesByMessageId(request)));
    }

    @GetMapping("/bySender")
    public BaseResponse<List<PrivateMessageVO>> getMessagesBySender(QueryMessagesBySenderRequest request) {
        return ResultUtils.success(privateMessagesService.convertToVOList(privateMessagesService.getMessagesBySender(request)));
    }

    @GetMapping("/search")
    public BaseResponse<List<PrivateMessageVO>> searchMessages(SearchMessagesRequest request) {
        return ResultUtils.success(privateMessagesService.convertToVOList(privateMessagesService.searchMessages(request)));
    }

    @GetMapping("/id/{id}")
    public BaseResponse<PrivateMessageVO> getMessageById(Integer id) {
        return ResultUtils.success(privateMessagesService.convertToVO(privateMessagesService.getMessageById(id)));
    }
}
