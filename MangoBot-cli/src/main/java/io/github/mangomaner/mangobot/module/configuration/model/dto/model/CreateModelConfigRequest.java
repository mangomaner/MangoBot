package io.github.mangomaner.mangobot.module.configuration.model.dto.model;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 创建模型配置请求
 */
@Data
@Schema(description = "创建模型配置请求")
public class CreateModelConfigRequest {

    @NotBlank(message = "模型名称不能为空")
    @Schema(description = "模型名称", example = "gpt-4o")
    private String modelName;

    @NotNull(message = "供应商ID不能为空")
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
}
