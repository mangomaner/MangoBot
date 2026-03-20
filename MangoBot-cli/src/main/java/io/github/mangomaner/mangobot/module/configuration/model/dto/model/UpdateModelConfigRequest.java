package io.github.mangomaner.mangobot.module.configuration.model.dto.model;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 更新模型配置请求
 */
@Data
@Schema(description = "更新模型配置请求")
public class UpdateModelConfigRequest {

    @Schema(description = "模型配置ID")
    private Long id;

    @Schema(description = "模型名称", example = "gpt-4o")
    private String modelName;

    @Schema(description = "供应商ID", example = "1")
    private Long providerId;

    @Schema(description = "温度参数", example = "0.7")
    private Double temperature;

    @Schema(description = "最大Token数", example = "4096")
    private Integer maxTokens;

    @Schema(description = "Top-P参数", example = "0.9")
    private Double topP;

    @Schema(description = "超时时间（秒）", example = "60")
    private Integer timeout;

    @Schema(description = "描述")
    private String description;

    @Schema(description = "是否启用", example = "true")
    private Boolean isEnabled;
}
