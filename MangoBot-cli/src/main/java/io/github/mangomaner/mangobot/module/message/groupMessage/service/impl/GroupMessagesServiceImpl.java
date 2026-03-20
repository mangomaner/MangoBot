package io.github.mangomaner.mangobot.module.message.groupMessage.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.mangomaner.mangobot.adapter.onebot.model.segment.OneBotMessageSegment;
import io.github.mangomaner.mangobot.module.message.model.domain.GroupMessages;
import io.github.mangomaner.mangobot.module.message.model.dto.QueryLatestMessagesRequest;
import io.github.mangomaner.mangobot.module.message.model.dto.QueryMessagesByMessageIdRequest;
import io.github.mangomaner.mangobot.module.message.model.dto.QueryMessagesBySenderRequest;
import io.github.mangomaner.mangobot.module.message.model.dto.SearchMessagesRequest;
import io.github.mangomaner.mangobot.module.message.model.dto.UpdateMessageRequest;
import io.github.mangomaner.mangobot.module.message.model.vo.GroupMessageVO;
import io.github.mangomaner.mangobot.module.message.groupMessage.service.GroupMessagesService;
import io.github.mangomaner.mangobot.system.mapper.GroupMessagesMapper;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

/**
* @author mangoman
* @description 针对表【group_messages】的数据库操作Service实现
* @createDate 2026-01-17 18:03:14
*/
@Service
public class GroupMessagesServiceImpl extends ServiceImpl<GroupMessagesMapper, GroupMessages>
    implements GroupMessagesService{

    private static final int PAGE_SIZE = 25;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public List<GroupMessages> getLatestMessages(QueryLatestMessagesRequest request) {
        LambdaQueryWrapper<GroupMessages> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(GroupMessages::getBotId, request.getBotId())
                .eq(GroupMessages::getGroupId, request.getTargetId())
                .orderByDesc(GroupMessages::getMessageTime)
                .last("LIMIT " + (request.getNum() == null ? PAGE_SIZE : request.getNum()));
        return this.list(wrapper);
    }

    @Override
    public List<GroupMessages> getMessagesByMessageId(QueryMessagesByMessageIdRequest request) {
        LambdaQueryWrapper<GroupMessages> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(GroupMessages::getBotId, request.getBotId())
                .eq(GroupMessages::getGroupId, request.getTargetId())
                .lt(GroupMessages::getMessageTime, 
                    this.getOne(new LambdaQueryWrapper<GroupMessages>()
                            .eq(GroupMessages::getMessageId, request.getMessageId()))
                            .getMessageTime())
                .orderByDesc(GroupMessages::getMessageTime)
                .last("LIMIT " + (request.getNum() == null ? PAGE_SIZE : request.getNum()));
        return this.list(wrapper);
    }

    @Override
    public List<GroupMessages> getMessagesBySender(QueryMessagesBySenderRequest request) {
        LambdaQueryWrapper<GroupMessages> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(GroupMessages::getBotId, request.getBotId())
                .eq(GroupMessages::getSenderId, request.getSenderId())
                .orderByDesc(GroupMessages::getMessageTime)
                .last("LIMIT " + PAGE_SIZE);
        return this.list(wrapper);
    }

    @Override
    public List<GroupMessages> searchMessages(SearchMessagesRequest request) {
        LambdaQueryWrapper<GroupMessages> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(GroupMessages::getBotId, request.getBotId())
                .eq(GroupMessages::getGroupId, request.getTargetId())
                .like(GroupMessages::getParseMessage, request.getKeyword())
                .orderByDesc(GroupMessages::getMessageTime);
        return this.list(wrapper);
    }

    @Override
    public GroupMessages getMessageById(Integer id) {
        return this.getById(id);
    }

    @Override
    public GroupMessages addGroupMessage(GroupMessages groupMessages) {
        this.save(groupMessages);
        return groupMessages;
    }

    @Override
    public Boolean deleteMessage(Integer id) {
        GroupMessages message = this.getById(id);
        if (message == null) {
            return false;
        }
        message.setIsDelete(1);
        return this.updateById(message);
    }

    @Override
    public Boolean updateMessage(UpdateMessageRequest request) {
        GroupMessages message = this.getById(request.getId());
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
    public List<GroupMessageVO> convertToVOList(List<GroupMessages> messages) {
        return messages.stream()
                .map(this::convertToVO)
                .collect(Collectors.toList());
    }

    @Override
    public GroupMessageVO convertToVO(GroupMessages message) {
        if (message == null) {
            return null;
        }
        GroupMessageVO vo = new GroupMessageVO();
        vo.setId(message.getId());
        vo.setBotId(message.getBotId());
        vo.setGroupId(message.getGroupId());
        vo.setMessageId(message.getMessageId());
        vo.setSenderId(message.getSenderId());
        vo.setMessageTime(message.getMessageTime());
        vo.setDeleted(message.getIsDelete());
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




