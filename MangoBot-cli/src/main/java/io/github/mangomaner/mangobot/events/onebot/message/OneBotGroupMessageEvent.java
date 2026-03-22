package io.github.mangomaner.mangobot.events.onebot.message;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeName;
import io.github.mangomaner.mangobot.adapter.onebot.model.event.message.OneBotMessageEvent;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@JsonTypeName("group")
public class OneBotGroupMessageEvent extends OneBotMessageEvent {
    @JsonProperty("group_id")
    private long groupId;

    @JsonProperty("group_name")
    private String groupName;

    private String parsedMessage;
}
