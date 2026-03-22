package io.github.mangomaner.mangobot.adapter.onebot.model.segment;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class FaceSegment extends OneBotMessageSegment {
    private FaceData data;

    @Data
    public static class FaceData {
        private String id;
        @JsonProperty("sub_type")
        private int subType;
        private String raw;     // NapCat
    }
}
