package io.github.mangomaner.mangobot.configuration.model.dto.plugin;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 更新插件配置请求
 */
@Data
@Schema(description = "更新插件配置请求")
public class UpdatePluginConfigRequest {

    @Schema(description = "配置ID")
    private Long id;

    @Schema(description = "配置键（相对于插件）", example = "timeout")
    private String configKey;

    @Schema(description = "配置值")
    private String configValue;

    @Schema(description = "配置类型", example = "STRING")
    private String configType;

    @Schema(description = "描述")
    private String description;

    @Schema(description = "详细说明")
    private String explain;
}
