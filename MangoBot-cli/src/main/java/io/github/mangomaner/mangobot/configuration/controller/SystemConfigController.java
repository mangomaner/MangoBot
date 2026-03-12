package io.github.mangomaner.mangobot.configuration.controller;

import io.github.mangomaner.mangobot.common.BaseResponse;
import io.github.mangomaner.mangobot.common.ResultUtils;
import io.github.mangomaner.mangobot.configuration.model.dto.system.CreateSystemConfigRequest;
import io.github.mangomaner.mangobot.configuration.model.dto.system.UpdateSystemConfigRequest;
import io.github.mangomaner.mangobot.configuration.model.vo.SystemConfigVO;
import io.github.mangomaner.mangobot.configuration.service.SystemConfigService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;

import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/configuration/system")
@Tag(name = "系统配置管理", description = "系统配置的增删改查接口")
public class SystemConfigController {

    @Resource
    private SystemConfigService systemConfigService;

    @GetMapping
    @Operation(summary = "获取所有系统配置", description = "获取所有默认系统配置列表（bot_id 为 null）")
    public BaseResponse<List<SystemConfigVO>> getAllConfigs() {
        return ResultUtils.success(systemConfigService.getAllConfigs());
    }

    @GetMapping("/bot/{botId}")
    @Operation(summary = "根据Bot ID获取配置", description = "获取指定Bot的配置（懒加载模式：返回默认配置和Bot专属配置，优先展示Bot专属配置）")
    public BaseResponse<List<SystemConfigVO>> getConfigsByBotId(
            @Parameter(description = "Bot ID") @PathVariable Long botId) {
        return ResultUtils.success(systemConfigService.getConfigsByBotId(botId));
    }

    @GetMapping("/category/{category}")
    @Operation(summary = "按分类获取配置", description = "根据分类获取默认系统配置列表")
    public BaseResponse<List<SystemConfigVO>> getConfigsByCategory(@PathVariable String category) {
        return ResultUtils.success(systemConfigService.getConfigsByCategory(category));
    }

    @GetMapping("/{configKey}")
    @Operation(summary = "根据Key获取配置", description = "根据配置键获取默认系统配置详情")
    public BaseResponse<SystemConfigVO> getConfigByKey(@PathVariable String configKey) {
        SystemConfigVO config = systemConfigService.getConfigByKey(configKey);
        return config != null ? ResultUtils.success(config) : ResultUtils.error(404, "配置不存在");
    }

    @GetMapping("/{configKey}/bot/{botId}")
    @Operation(summary = "根据Key和Bot ID获取配置", description = "根据配置键和Bot ID获取配置（优先返回Bot专属配置）")
    public BaseResponse<SystemConfigVO> getConfigByKeyAndBotId(
            @PathVariable String configKey,
            @Parameter(description = "Bot ID") @PathVariable Long botId) {
        SystemConfigVO config = systemConfigService.getConfigByKeyAndBotId(configKey, botId);
        return config != null ? ResultUtils.success(config) : ResultUtils.error(404, "配置不存在");
    }

    @PostMapping
    @Operation(summary = "创建系统配置", description = "创建新的系统配置")
    public BaseResponse<SystemConfigVO> createConfig(@Valid @RequestBody CreateSystemConfigRequest request) {
        return ResultUtils.success(systemConfigService.createConfig(request));
    }

    @PutMapping
    @Operation(summary = "更新系统配置", description = "更新系统配置信息")
    public BaseResponse<SystemConfigVO> updateConfig(@RequestBody UpdateSystemConfigRequest request) {
        SystemConfigVO config = systemConfigService.updateConfig(request);
        return config != null ? ResultUtils.success(config) : ResultUtils.error(404, "配置不存在");
    }

    @PutMapping("/{configKey}")
    @Operation(summary = "更新配置值", description = "根据配置键更新默认配置值")
    public BaseResponse<Boolean> updateConfigValue(@PathVariable String configKey,
                                                    @RequestBody String configValue) {
        return ResultUtils.success(systemConfigService.updateConfigValue(configKey, configValue));
    }

    @PutMapping("/{configKey}/bot/{botId}")
    @Operation(summary = "更新Bot专属配置值", description = "根据配置键和Bot ID更新配置值（懒加载：若Bot专属配置不存在则创建）")
    public BaseResponse<Boolean> updateConfigValueByBotId(
            @PathVariable String configKey,
            @Parameter(description = "Bot ID") @PathVariable Long botId,
            @RequestBody String configValue) {
        return ResultUtils.success(systemConfigService.updateConfigValue(configKey, botId, configValue));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "删除系统配置", description = "删除指定的系统配置")
    public BaseResponse<Boolean> deleteConfig(@PathVariable Long id) {
        return ResultUtils.success(systemConfigService.deleteConfig(id));
    }
}
