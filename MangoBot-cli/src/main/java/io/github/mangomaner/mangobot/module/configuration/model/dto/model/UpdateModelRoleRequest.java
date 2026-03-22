package io.github.mangomaner.mangobot.module.configuration.model.dto.model;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 更新模型角色请求
 */
@Data
@Schema(description = "更新模型角色请求")
public class UpdateModelRoleRequest {

    @NotNull(message = "模型配置ID不能为空")
    @Schema(description = "模型配置ID", example = "1")
    private Long modelConfigId;
}
