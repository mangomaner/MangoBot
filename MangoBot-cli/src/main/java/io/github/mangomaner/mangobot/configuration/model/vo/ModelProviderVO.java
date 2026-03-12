package io.github.mangomaner.mangobot.configuration.model.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 模型供应商视图对象
 */
@Data
@Schema(description = "模型供应商信息")
public class ModelProviderVO {

    @Schema(description = "供应商ID")
    private Long id;

    @Schema(description = "供应商名称")
    private String name;

    @Schema(description = "API 基础地址")
    private String baseUrl;

    @Schema(description = "API 密钥（脱敏）")
    private String apiKey;

    @Schema(description = "默认超时时间（秒）")
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
