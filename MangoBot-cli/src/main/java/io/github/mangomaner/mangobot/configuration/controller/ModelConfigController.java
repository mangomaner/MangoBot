package io.github.mangomaner.mangobot.configuration.controller;

import io.github.mangomaner.mangobot.common.BaseResponse;
import io.github.mangomaner.mangobot.common.ResultUtils;
import io.github.mangomaner.mangobot.configuration.model.dto.model.CreateModelConfigRequest;
import io.github.mangomaner.mangobot.configuration.model.dto.model.CreateModelProviderRequest;
import io.github.mangomaner.mangobot.configuration.model.dto.model.TestModelRequest;
import io.github.mangomaner.mangobot.configuration.model.dto.model.UpdateModelConfigRequest;
import io.github.mangomaner.mangobot.configuration.model.dto.model.UpdateModelProviderRequest;
import io.github.mangomaner.mangobot.configuration.model.vo.ModelConfigVO;
import io.github.mangomaner.mangobot.configuration.model.vo.ModelProviderVO;
import io.github.mangomaner.mangobot.configuration.model.vo.ModelTestResultVO;
import io.github.mangomaner.mangobot.configuration.service.ModelConfigService;
import io.github.mangomaner.mangobot.configuration.service.ModelProviderService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;

import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 模型配置控制器
 * 提供模型供应商和模型配置的管理接口
 */
@RestController
@RequestMapping("/api/configuration/model")
@Tag(name = "模型配置管理", description = "模型供应商和模型配置的增删改查接口")
public class ModelConfigController {

    @Resource
    private ModelProviderService modelProviderService;

    @Resource
    private ModelConfigService modelConfigService;

    // ==================== 供应商管理 ====================

    @GetMapping("/providers")
    @Operation(summary = "获取所有供应商", description = "获取所有模型供应商列表")
    public BaseResponse<List<ModelProviderVO>> getAllProviders() {
        return ResultUtils.success(modelProviderService.getAllProviders());
    }

    @GetMapping("/providers/{id}")
    @Operation(summary = "获取供应商详情", description = "根据ID获取供应商详情")
    public BaseResponse<ModelProviderVO> getProviderById(@PathVariable Long id) {
        ModelProviderVO provider = modelProviderService.getProviderById(id);
        return provider != null ? ResultUtils.success(provider) : ResultUtils.error(404, "供应商不存在");
    }

    @PostMapping("/providers")
    @Operation(summary = "创建供应商", description = "创建新的模型供应商")
    public BaseResponse<ModelProviderVO> createProvider(@Valid @RequestBody CreateModelProviderRequest request) {
        return ResultUtils.success(modelProviderService.createProvider(request));
    }

    @PutMapping("/providers")
    @Operation(summary = "更新供应商", description = "更新模型供应商信息")
    public BaseResponse<ModelProviderVO> updateProvider(@RequestBody UpdateModelProviderRequest request) {
        ModelProviderVO provider = modelProviderService.updateProvider(request);
        return provider != null ? ResultUtils.success(provider) : ResultUtils.error(404, "供应商不存在");
    }

    @DeleteMapping("/providers/{id}")
    @Operation(summary = "删除供应商", description = "删除指定的模型供应商")
    public BaseResponse<Boolean> deleteProvider(@PathVariable Long id) {
        return ResultUtils.success(modelProviderService.deleteProvider(id));
    }

    @PostMapping("/providers/{id}/test")
    @Operation(summary = "测试供应商连接", description = "测试供应商的API连接是否正常")
    public BaseResponse<Boolean> testProviderConnection(@PathVariable Long id) {
        return ResultUtils.success(modelProviderService.testConnection(id));
    }

    // ==================== 模型配置管理 ====================

    @GetMapping("/configs")
    @Operation(summary = "获取所有模型配置", description = "获取所有模型配置列表")
    public BaseResponse<List<ModelConfigVO>> getAllConfigs() {
        return ResultUtils.success(modelConfigService.getAllConfigs());
    }

    @GetMapping("/configs/{id}")
    @Operation(summary = "获取模型配置详情", description = "根据ID获取模型配置详情")
    public BaseResponse<ModelConfigVO> getConfigById(@PathVariable Long id) {
        ModelConfigVO config = modelConfigService.getConfigById(id);
        return config != null ? ResultUtils.success(config) : ResultUtils.error(404, "模型配置不存在");
    }

    @PostMapping("/configs")
    @Operation(summary = "创建模型配置", description = "创建新的模型配置")
    public BaseResponse<ModelConfigVO> createConfig(@Valid @RequestBody CreateModelConfigRequest request) {
        return ResultUtils.success(modelConfigService.createConfig(request));
    }

    @PutMapping("/configs")
    @Operation(summary = "更新模型配置", description = "更新模型配置信息")
    public BaseResponse<ModelConfigVO> updateConfig(@RequestBody UpdateModelConfigRequest request) {
        ModelConfigVO config = modelConfigService.updateConfig(request);
        return config != null ? ResultUtils.success(config) : ResultUtils.error(404, "模型配置不存在");
    }

    @DeleteMapping("/configs/{id}")
    @Operation(summary = "删除模型配置", description = "删除指定的模型配置")
    public BaseResponse<Boolean> deleteConfig(@PathVariable Long id) {
        return ResultUtils.success(modelConfigService.deleteConfig(id));
    }

    @PostMapping("/configs/{id}/test")
    @Operation(summary = "测试模型", description = "测试模型是否可用")
    public BaseResponse<ModelTestResultVO> testModel(@PathVariable Long id,
                                                      @RequestBody(required = false) TestModelRequest request) {
        if (request == null) {
            request = new TestModelRequest();
        }
        return ResultUtils.success(modelConfigService.testModel(id, request));
    }
}
