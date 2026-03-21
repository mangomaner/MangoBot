package io.github.mangomaner.mangobot.adapter.message_handler.onebot.event;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import io.github.mangomaner.mangobot.adapter.message_handler.onebot.inbound.json_to_event.OneBotEventDeserializer;
import lombok.Data;

@Data
@JsonDeserialize(using = OneBotEventDeserializer.class)
public abstract class OneBotBaseEvent implements OneBotEvent {
    private long time;
    
    @JsonProperty("self_id")
    private long selfId;
    
    @JsonProperty("post_type")
    private String postType;
}
