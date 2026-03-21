package io.github.mangomaner.mangobot.module.message.privateMessage.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.mangomaner.mangobot.system.mapper.PrivateMessagesMapper;
import io.github.mangomaner.mangobot.module.message.model.domain.PrivateMessages;
import io.github.mangomaner.mangobot.adapter.message_handler.onebot.model.segment.OneBotMessageSegment;
import io.github.mangomaner.mangobot.module.message.model.vo.PrivateMessageVO;
import io.github.mangomaner.mangobot.module.message.model.dto.*;
import io.github.mangomaner.mangobot.module.message.privateMessage.service.PrivateMessagesService;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

/**
* @author mangoman
* @description 针对表【private_messages】的数据库操作Service实现
* @createDate 2026-01-17 18:03:57
*/
@Service
public class PrivateMessagesServiceImpl extends ServiceImpl<PrivateMessagesMapper, PrivateMessages>
    implements PrivateMessagesService {

    private static final int PAGE_SIZE = 25;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public List<PrivateMessages> getLatestMessages(QueryLatestMessagesRequest request) {
        LambdaQueryWrapper<PrivateMessages> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(PrivateMessages::getBotId, request.getBotId())
                .eq(PrivateMessages::getFriendId, request.getTargetId())
                .orderByDesc(PrivateMessages::getMessageTime)
                .last("LIMIT " + (request.getNum() == null ? PAGE_SIZE : request.getNum()));
        return this.list(wrapper);
    }

    @Override
    public List<PrivateMessages> getMessagesByMessageId(QueryMessagesByMessageIdRequest request) {
        LambdaQueryWrapper<PrivateMessages> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(PrivateMessages::getBotId, request.getBotId())
                .eq(PrivateMessages::getFriendId, request.getTargetId())
                .lt(PrivateMessages::getMessageTime, 
                    this.getOne(new LambdaQueryWrapper<PrivateMessages>()
                            .eq(PrivateMessages::getMessageId, request.getMessageId()))
                            .getMessageTime())
                .orderByDesc(PrivateMessages::getMessageTime)
                .last("LIMIT " + (request.getNum() == null ? PAGE_SIZE : request.getNum()));
        return this.list(wrapper);
    }

    @Override
    public List<PrivateMessages> getMessagesBySender(QueryMessagesBySenderRequest request) {
        LambdaQueryWrapper<PrivateMessages> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(PrivateMessages::getBotId, request.getBotId())
                .eq(PrivateMessages::getSenderId, request.getSenderId())
                .orderByDesc(PrivateMessages::getMessageTime)
                .last("LIMIT " + PAGE_SIZE);
        return this.list(wrapper);
    }

    @Override
    public List<PrivateMessages> searchMessages(SearchMessagesRequest request) {
        LambdaQueryWrapper<PrivateMessages> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(PrivateMessages::getBotId, request.getBotId())
                .eq(PrivateMessages::getFriendId, request.getTargetId())
                .like(PrivateMessages::getParseMessage, request.getKeyword())
                .orderByDesc(PrivateMessages::getMessageTime);
        return this.list(wrapper);
    }

    @Override
    public PrivateMessages getMessageById(Integer id) {
        return this.getById(id);
    }

    @Override
    public PrivateMessages addPrivateMessage(PrivateMessages privateMessages) {
        this.save(privateMessages);
        return privateMessages;
    }

    @Override
    public Boolean deleteMessage(Integer id) {
        PrivateMessages message = this.getById(id);
        if (message == null) {
            return false;
        }
        message.setIsDelete(1);
        return this.updateById(message);
    }

    @Override
    public Boolean updateMessage(UpdateMessageRequest request) {
        PrivateMessages message = this.getById(request.getId());
        if (message == null) {
            return false;
        }
        if (request.getMessageSegments() != null) {
            message.setMessageSegments(request.getMessageSegments());
        }
        if (request.getParseMessage() != null) {
            message.setParseMessage(request.getParseMessage());
        }
        return this.updateById(message);
    }

    @Override
    public List<PrivateMessageVO> convertToVOList(List<PrivateMessages> messages) {
        return messages.stream()
                .map(this::convertToVO)
                .collect(Collectors.toList());
    }

    @Override
    public PrivateMessageVO convertToVO(PrivateMessages message) {
        if (message == null) {
            return null;
        }
        PrivateMessageVO vo = new PrivateMessageVO();
        vo.setId(message.getId());
        vo.setBotId(message.getBotId());
        vo.setFriendId(message.getFriendId());
        vo.setMessageId(message.getMessageId());
        vo.setSenderId(message.getSenderId());
        vo.setMessageTime(message.getMessageTime());
        vo.setIsDelete(message.getIsDelete());
        vo.setParseMessage(message.getParseMessage());
        
        try {
            List<OneBotMessageSegment> segments = objectMapper.readValue(
                message.getMessageSegments(),
                new TypeReference<List<OneBotMessageSegment>>() {}
            );
            vo.setMessageSegments(segments);
        } catch (Exception e) {
            vo.setMessageSegments(null);
        }
        
        return vo;
    }
}
