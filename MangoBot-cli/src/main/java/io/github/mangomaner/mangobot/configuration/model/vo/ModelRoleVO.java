package io.github.mangomaner.mangobot.configuration.model.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 模型角色视图对象
 */
@Data
@Schema(description = "模型角色信息")
public class ModelRoleVO {

    @Schema(description = "角色ID")
    private Long id;

    @Schema(description = "角色标识")
    private String roleKey;

    @Schema(description = "角色名称")
    private String roleName;

    @Schema(description = "关联的模型配置ID")
    private Long modelConfigId;

    @Schema(description = "模型名称")
    private String modelName;

    @Schema(description = "供应商名称")
    private String providerName;

    @Schema(description = "描述")
    private String description;

    @Schema(description = "创建时间")
    private Long createdAt;

    @Schema(description = "更新时间")
    private Long updatedAt;
}
