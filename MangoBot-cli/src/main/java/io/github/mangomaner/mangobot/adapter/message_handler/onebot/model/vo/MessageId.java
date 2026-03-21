package io.github.mangomaner.mangobot.adapter.message_handler.onebot.model.vo;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class MessageId {
    @JsonProperty("message_id")
    private int messageId;
}
