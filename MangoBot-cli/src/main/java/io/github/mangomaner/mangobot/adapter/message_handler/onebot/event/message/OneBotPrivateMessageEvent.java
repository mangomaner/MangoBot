package io.github.mangomaner.mangobot.adapter.message_handler.onebot.event.message;

import com.fasterxml.jackson.annotation.JsonTypeName;
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
