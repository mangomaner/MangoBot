package io.github.mangomaner.mangobot.events.onebot.message;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import io.github.mangomaner.mangobot.adapter.onebot.model.event.message.OneBotMessageEvent;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

/**
 * bot自己发送的消息（由用户在另一台设备上发送的消息，不是由本程序发送）
 */
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@JsonDeserialize(using = com.fasterxml.jackson.databind.JsonDeserializer.None.class)
public class OneBotMessageSentEvent extends OneBotMessageEvent {
    @JsonProperty("message_sent_type")
    private String messageSentType;

    @JsonProperty("group_id")
    private Long groupId;

    @JsonProperty("group_name")
    private String groupName;   // NapCat

    @JsonProperty("target_id")
    private Long targetId;
}
