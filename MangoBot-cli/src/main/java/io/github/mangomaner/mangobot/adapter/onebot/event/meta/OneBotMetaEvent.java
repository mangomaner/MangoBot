package io.github.mangomaner.mangobot.adapter.onebot.event.meta;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import io.github.mangomaner.mangobot.adapter.onebot.event.OneBotBaseEvent;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@JsonDeserialize(using = JsonDeserializer.None.class)
public abstract class OneBotMetaEvent extends OneBotBaseEvent {
    @JsonProperty("meta_event_type")
    private String metaEventType;
}
