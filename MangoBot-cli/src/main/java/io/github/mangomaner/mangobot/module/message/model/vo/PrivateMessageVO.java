package io.github.mangomaner.mangobot.module.message.model.vo;

import io.github.mangomaner.mangobot.adapter.onebot.model.segment.OneBotMessageSegment;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

@Data
@Schema(description = "私聊消息返回")
public class PrivateMessageVO {

    @Schema(description = "消息ID")
    private Long id;

    @Schema(description = "Bot ID")
    private String botId;

    @Schema(description = "好友ID")
    private String friendId;

    @Schema(description = "消息ID")
    private String messageId;

    @Schema(description = "发送者ID")
    private String senderId;

    @Schema(description = "消息段列表")
    private List<OneBotMessageSegment> messageSegments;

    @Schema(description = "消息时间")
    private Long messageTime;

    @Schema(description = "是否删除")
    private Integer isDelete;

    @Schema(description = "解析后的消息")
    private String parseMessage;
}
