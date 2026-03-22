package io.github.mangomaner.mangobot.module.configuration.model.dto.model;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 创建模型供应商请求
 */
@Data
@Schema(description = "创建模型供应商请求")
public class CreateModelProviderRequest {

    @NotBlank(message = "供应商名称不能为空")
    @Schema(description = "供应商名称", example = "openai")
    private String name;

    @NotBlank(message = "API 基础地址不能为空")
    @Schema(description = "API 基础地址", example = "https://api.openai.com/v1")
    private String baseUrl;

    @NotBlank(message = "API 密钥不能为空")
    @Schema(description = "API 密钥", example = "sk-xxx")
    private String apiKey;

    @Schema(description = "默认超时时间（秒）", example = "30")
    private Integer timeout;

    @Schema(description = "描述")
    private String description;
}
