package io.github.mangomaner.mangobot.events.onebot.notice;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import io.github.mangomaner.mangobot.adapter.onebot.model.event.notice.OneBotNoticeEvent;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@JsonDeserialize(using = com.fasterxml.jackson.databind.JsonDeserializer.None.class)
public class OneBotGroupUploadEvent extends OneBotNoticeEvent {
    @JsonProperty("group_id")
    private long groupId;

    @JsonProperty("user_id")
    private long userId;

    private FileInfo file;

    @Data
    public static class FileInfo {
        private String id;
        private String name;
        private long size;
        private Integer busid;
    }
}
