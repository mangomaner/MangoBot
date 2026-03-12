package io.github.mangomaner.mangobot.configuration.controller;

import io.github.mangomaner.mangobot.common.BaseResponse;
import io.github.mangomaner.mangobot.common.ResultUtils;
import io.github.mangomaner.mangobot.configuration.model.dto.plugin.UpdatePluginConfigRequest;
import io.github.mangomaner.mangobot.configuration.model.vo.PluginConfigVO;
import io.github.mangomaner.mangobot.configuration.service.PluginConfigService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 插件配置控制器
 * 提供插件配置的管理接口
 */
@RestController
@RequestMapping("/api/configuration/plugin")
@Tag(name = "插件配置管理", description = "插件配置的查询和更新接口")
public class PluginConfigController {

    @Resource
    private PluginConfigService pluginConfigService;

    @GetMapping
    @Operation(summary = "获取所有插件配置", description = "获取所有插件配置列表")
    public BaseResponse<List<PluginConfigVO>> getAllConfigs() {
        return ResultUtils.success(pluginConfigService.getAllConfigs());
    }

    @GetMapping("/{pluginId}")
    @Operation(summary = "获取插件的配置列表", description = "根据插件ID获取该插件的所有配置")
    public BaseResponse<List<PluginConfigVO>> getConfigsByPluginId(@PathVariable Long pluginId) {
        return ResultUtils.success(pluginConfigService.getConfigsByPluginId(pluginId));
    }

    @GetMapping("/{pluginId}/{configKey}")
    @Operation(summary = "获取单个插件配置", description = "根据插件ID和配置键获取配置详情")
    public BaseResponse<PluginConfigVO> getConfig(@PathVariable Long pluginId,
                                                   @PathVariable String configKey) {
        PluginConfigVO config = pluginConfigService.getConfig(pluginId, configKey);
        return config != null ? ResultUtils.success(config) : ResultUtils.error(404, "配置不存在");
    }

    @PutMapping
    @Operation(summary = "更新插件配置", description = "更新插件配置信息")
    public BaseResponse<PluginConfigVO> updateConfig(@RequestBody UpdatePluginConfigRequest request) {
        PluginConfigVO config = pluginConfigService.updateConfig(request);
        return config != null ? ResultUtils.success(config) : ResultUtils.error(404, "配置不存在");
    }

    @PutMapping("/{pluginId}/{configKey}")
    @Operation(summary = "更新插件配置值", description = "根据插件ID和配置键更新配置值")
    public BaseResponse<Boolean> updateConfigValue(@PathVariable Long pluginId,
                                                    @PathVariable String configKey,
                                                    @RequestBody String configValue) {
        return ResultUtils.success(pluginConfigService.updateConfigValue(pluginId, configKey, configValue));
    }
}
