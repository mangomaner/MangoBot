package io.github.mangomaner.mangobot.events.onebot.meta;

import com.fasterxml.jackson.annotation.JsonTypeName;
import io.github.mangomaner.mangobot.adapter.onebot.model.event.meta.OneBotMetaEvent;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@JsonTypeName("heartbeat")
public class OneBotHeartbeatEvent extends OneBotMetaEvent {
    private long interval;
    private Status status;

    @Data
    public static class Status {
        private boolean online;
        private boolean good;
    }
}
