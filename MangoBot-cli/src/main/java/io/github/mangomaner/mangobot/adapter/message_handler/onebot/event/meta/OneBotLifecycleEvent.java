package io.github.mangomaner.mangobot.adapter.message_handler.onebot.event.meta;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeName;
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
