package io.github.mangomaner.mangobot.adapter.message_handler.onebot.model.segment;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class AtSegment extends OneBotMessageSegment {
    private AtData data;

    @Data
    public static class AtData {
        private String qq;
        private String name;
    }
}
