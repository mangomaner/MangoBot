package io.github.mangomaner.mangobot.events.onebot.message;

import com.fasterxml.jackson.annotation.JsonTypeName;
import io.github.mangomaner.mangobot.adapter.onebot.model.event.message.OneBotMessageEvent;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@JsonTypeName("private")
public class OneBotPrivateMessageEvent extends OneBotMessageEvent {
    // Private message specific fields if any
    private String parsedMessage;
}
