package io.github.mangomaner.mangobot.adapter.message_handler.onebot.model.segment;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class JsonSegment extends OneBotMessageSegment {
    private JsonData data;

    @Data
    public static class JsonData {
        private String data; // The JSON string content
    }
}
