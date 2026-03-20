package io.github.mangomaner.mangobot.adapter.onebot.model.segment;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class ReplySegment extends OneBotMessageSegment {
    private ReplyData data;

    @Data
    public static class ReplyData {
        private String id;
    }
}
