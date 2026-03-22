package io.github.mangomaner.mangobot.adapter.model.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "平台选项视图对象")
public class PlatformOptionVO {

    @Schema(description = "平台代码")
    private String code;

    @Schema(description = "平台名称")
    private String name;

    @Schema(description = "平台描述")
    private String description;
}
