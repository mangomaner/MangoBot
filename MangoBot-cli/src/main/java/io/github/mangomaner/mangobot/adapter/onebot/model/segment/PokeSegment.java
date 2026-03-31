package io.github.mangomaner.mangobot.adapter.onebot.model.segment;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class PokeSegment extends OneBotMessageSegment {
    private PokeData data;

    @Data
    public static class PokeData {
        private String type;
        private String id;
        private String name;
    }
}
