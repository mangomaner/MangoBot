package io.github.mangomaner.mangobot.configuration.controller;

import io.github.mangomaner.mangobot.common.BaseResponse;
import io.github.mangomaner.mangobot.common.ResultUtils;
import io.github.mangomaner.mangobot.configuration.model.dto.model.UpdateModelRoleRequest;
import io.github.mangomaner.mangobot.configuration.model.vo.ModelRoleVO;
import io.github.mangomaner.mangobot.configuration.service.ModelProvider;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;

import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 模型角色控制器
 * 管理模型角色与模型配置的映射关系
 */
@RestController
@RequestMapping("/api/configuration/model/roles")
@Tag(name = "模型角色管理", description = "模型角色的查询和更新接口")
public class ModelRoleController {

    @Resource
    private ModelProvider modelProvider;

    @GetMapping
    @Operation(summary = "获取所有角色", description = "获取所有模型角色及其关联的模型配置")
    public BaseResponse<List<ModelRoleVO>> getAllRoles() {
        return ResultUtils.success(modelProvider.getAllRoles());
    }

    @GetMapping("/{roleKey}")
    @Operation(summary = "获取角色详情", description = "根据角色标识获取角色详情")
    public BaseResponse<ModelRoleVO> getRoleByKey(@PathVariable String roleKey) {
        ModelRoleVO role = modelProvider.getRole(roleKey);
        return role != null ? ResultUtils.success(role) : ResultUtils.error(404, "角色不存在");
    }

    @PutMapping("/{roleKey}")
    @Operation(summary = "更新角色模型", description = "更新指定角色关联的模型配置")
    public BaseResponse<ModelRoleVO> updateRoleModel(
            @PathVariable String roleKey,
            @Valid @RequestBody UpdateModelRoleRequest request) {
        modelProvider.updateRoleModel(roleKey, request.getModelConfigId());
        ModelRoleVO role = modelProvider.getRole(roleKey);
        return role != null ? ResultUtils.success(role) : ResultUtils.error(404, "角色不存在");
    }
}
