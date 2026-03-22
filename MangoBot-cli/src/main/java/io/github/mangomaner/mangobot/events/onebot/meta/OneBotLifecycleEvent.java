package io.github.mangomaner.mangobot.events.onebot.meta;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeName;
import io.github.mangomaner.mangobot.adapter.onebot.model.event.meta.OneBotMetaEvent;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@JsonTypeName("lifecycle")
public class OneBotLifecycleEvent extends OneBotMetaEvent {
    @JsonProperty("sub_type")
    private String subType; // connect, etc.
}
