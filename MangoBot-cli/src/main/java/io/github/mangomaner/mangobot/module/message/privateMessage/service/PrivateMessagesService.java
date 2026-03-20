package io.github.mangomaner.mangobot.module.message.privateMessage.service;

import io.github.mangomaner.mangobot.module.message.model.domain.PrivateMessages;
import io.github.mangomaner.mangobot.module.message.model.dto.QueryLatestMessagesRequest;
import io.github.mangomaner.mangobot.module.message.model.dto.QueryMessagesByMessageIdRequest;
import io.github.mangomaner.mangobot.module.message.model.dto.QueryMessagesBySenderRequest;
import io.github.mangomaner.mangobot.module.message.model.dto.SearchMessagesRequest;
import io.github.mangomaner.mangobot.module.message.model.dto.UpdateMessageRequest;
import io.github.mangomaner.mangobot.adapter.onebot.event.message.OneBotPrivateMessageEvent;
import io.github.mangomaner.mangobot.adapter.onebot.model.segment.OneBotMessageSegment;
import io.github.mangomaner.mangobot.module.message.model.vo.PrivateMessageVO;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

/**
* @author mangoman
* @description 针对表【private_messages】的数据库操作Service
* @createDate 2026-01-17 18:03:57
*/
public interface PrivateMessagesService extends IService<PrivateMessages> {

    List<PrivateMessages> getLatestMessages(QueryLatestMessagesRequest request);

    List<PrivateMessages> getMessagesByMessageId(QueryMessagesByMessageIdRequest request);

    List<PrivateMessages> getMessagesBySender(QueryMessagesBySenderRequest request);

    List<PrivateMessages> searchMessages(SearchMessagesRequest request);

    PrivateMessages getMessageById(Integer id);

    PrivateMessages addPrivateMessage(OneBotPrivateMessageEvent event);

    PrivateMessages addPrivateMessage(List<OneBotMessageSegment> segments, Long botId, Long friendId, Integer messageId);

    Boolean deleteMessage(Integer id);

    Boolean updateMessage(UpdateMessageRequest request);

    List<PrivateMessageVO> convertToVOList(List<PrivateMessages> messages);

    PrivateMessageVO convertToVO(PrivateMessages message);
}
