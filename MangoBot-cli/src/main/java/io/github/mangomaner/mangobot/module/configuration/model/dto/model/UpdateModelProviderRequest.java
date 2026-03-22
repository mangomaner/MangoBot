package io.github.mangomaner.mangobot.module.configuration.model.dto.model;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 更新模型供应商请求
 */
@Data
@Schema(description = "更新模型供应商请求")
public class UpdateModelProviderRequest {

    @Schema(description = "供应商ID")
    private Long id;

    @Schema(description = "供应商名称", example = "openai")
    private String name;

    @Schema(description = "API 基础地址", example = "https://api.openai.com/v1")
    private String baseUrl;

    @Schema(description = "API 密钥", example = "sk-xxx")
    private String apiKey;

    @Schema(description = "默认超时时间（秒）", example = "30")
    private Integer timeout;

    @Schema(description = "描述")
    private String description;

    @Schema(description = "是否启用", example = "true")
    private Boolean isEnabled;
}
