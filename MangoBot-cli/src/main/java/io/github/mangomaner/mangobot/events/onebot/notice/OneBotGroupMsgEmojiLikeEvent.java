package io.github.mangomaner.mangobot.events.onebot.notice;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import io.github.mangomaner.mangobot.adapter.onebot.model.event.notice.OneBotNoticeEvent;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.util.List;

@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@JsonDeserialize(using = com.fasterxml.jackson.databind.JsonDeserializer.None.class)
public class OneBotGroupMsgEmojiLikeEvent extends OneBotNoticeEvent {
    @JsonProperty("group_id")
    private long groupId;

    @JsonProperty("user_id")
    private long userId;

    @JsonProperty("message_id")
    private long messageId;

    private List<EmojiLike> likes;

    @JsonProperty("is_add")
    private Boolean isAdd;

    @Data
    public static class EmojiLike {
        @JsonProperty("emoji_id")
        private String emojiId;
        private Integer count;
    }
}
