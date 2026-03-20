package io.github.mangomaner.mangobot.adapter.onebot.model.segment;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class ImageSegment extends OneBotMessageSegment {
    private ImageData data;

    @Data
    public static class ImageData {
        private String file;
        @JsonProperty("sub_type")
        private Integer subType;
        private String url;
        @JsonProperty("file_size")
        private String fileSize;
        private String summary; // NapCat
    }
}
