package io.github.mangomaner.mangobot.module.configuration.model.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 模型配置视图对象
 */
@Data
@Schema(description = "模型配置信息")
public class ModelConfigVO {

    @Schema(description = "模型配置ID")
    private Long id;

    @Schema(description = "模型名称")
    private String modelName;

    @Schema(description = "供应商ID")
    private Long providerId;

    @Schema(description = "供应商名称")
    private String providerName;

    @Schema(description = "温度参数")
    private Double temperature;

    @Schema(description = "最大Token数")
    private Integer maxTokens;

    @Schema(description = "Top-P参数")
    private Double topP;

    @Schema(description = "超时时间（秒）")
    private Integer timeout;

    @Schema(description = "描述")
    private String description;

    @Schema(description = "是否启用")
    private Boolean isEnabled;

    @Schema(description = "创建时间")
    private Long createdAt;

    @Schema(description = "更新时间")
    private Long updatedAt;
}
