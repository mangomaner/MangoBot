package io.github.mangomaner.mangobot.module.configuration.model.dto.system;

import io.github.mangomaner.mangobot.module.configuration.model.config.ConfigMetadata;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
@Schema(description = "创建系统配置请求")
public class CreateSystemConfigRequest {

    @Schema(description = "Bot ID（null 表示默认配置）")
    private Long botId;

    @NotBlank(message = "配置键不能为空")
    @Schema(description = "配置键", example = "group.whitelist")
    private String configKey;

    @Schema(description = "配置值")
    private String configValue;

    @Schema(description = "配置类型", example = "STRING")
    private String configType;

    @Schema(description = "前端元数据（选项列表、范围限制等）")
    private ConfigMetadata metadata;

    @Schema(description = "描述")
    private String description;

    @Schema(description = "详细说明")
    private String explain;

    @Schema(description = "分类", example = "general")
    private String category;

    @Schema(description = "是否可编辑", example = "true")
    private Boolean editable;
}
