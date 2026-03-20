package io.github.mangomaner.mangobot.module.configuration.model.vo;

import io.github.mangomaner.mangobot.module.configuration.model.config.ConfigMetadata;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "插件配置信息")
public class PluginConfigVO {

    @Schema(description = "配置ID")
    private Long id;

    @Schema(description = "插件ID")
    private Long pluginId;

    @Schema(description = "Bot ID（null 表示默认配置）")
    private Long botId;

    @Schema(description = "插件名称")
    private String pluginName;

    @Schema(description = "配置键")
    private String configKey;

    @Schema(description = "配置值")
    private String configValue;

    @Schema(description = "配置类型")
    private String configType;

    @Schema(description = "前端元数据（选项列表、范围限制等）")
    private ConfigMetadata metadata;

    @Schema(description = "描述")
    private String description;

    @Schema(description = "详细说明")
    private String explain;

    @Schema(description = "是否可编辑")
    private Boolean editable;

    @Schema(description = "创建时间")
    private Long createdAt;

    @Schema(description = "更新时间")
    private Long updatedAt;
}
