package io.github.mangomaner.mangobot.module.configuration.controller;

import io.github.mangomaner.mangobot.system.common.BaseResponse;
import io.github.mangomaner.mangobot.system.common.ResultUtils;
import io.github.mangomaner.mangobot.module.configuration.model.dto.plugin.UpdatePluginConfigRequest;
import io.github.mangomaner.mangobot.module.configuration.model.vo.PluginConfigVO;
import io.github.mangomaner.mangobot.module.configuration.service.PluginConfigService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;

import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/configuration/plugin")
@Tag(name = "插件配置管理", description = "插件配置的查询和更新接口")
public class PluginConfigController {

    @Resource
    private PluginConfigService pluginConfigService;

    @GetMapping
    @Operation(summary = "获取所有插件配置", description = "获取所有默认插件配置列表（bot_id 为 null）")
    public BaseResponse<List<PluginConfigVO>> getAllConfigs() {
        return ResultUtils.success(pluginConfigService.getAllConfigs());
    }

    @GetMapping("/bot/{botId}")
    @Operation(summary = "根据Bot ID获取配置", description = "获取指定Bot的插件配置（懒加载模式）")
    public BaseResponse<List<PluginConfigVO>> getConfigsByBotId(
            @Parameter(description = "Bot ID") @PathVariable Long botId) {
        return ResultUtils.success(pluginConfigService.getConfigsByBotId(botId));
    }

    @GetMapping("/{pluginId}")
    @Operation(summary = "获取插件的配置列表", description = "根据插件ID获取该插件的默认配置")
    public BaseResponse<List<PluginConfigVO>> getConfigsByPluginId(@PathVariable Long pluginId) {
        return ResultUtils.success(pluginConfigService.getConfigsByPluginId(pluginId));
    }

    @GetMapping("/{pluginId}/bot/{botId}")
    @Operation(summary = "获取插件的Bot专属配置列表", description = "根据插件ID和Bot ID获取配置（懒加载模式）")
    public BaseResponse<List<PluginConfigVO>> getConfigsByPluginIdAndBotId(
            @PathVariable Long pluginId,
            @Parameter(description = "Bot ID") @PathVariable Long botId) {
        return ResultUtils.success(pluginConfigService.getConfigsByPluginIdAndBotId(pluginId, botId));
    }

    @GetMapping("/{pluginId}/{configKey}")
    @Operation(summary = "获取单个插件配置", description = "根据插件ID和配置键获取默认配置详情")
    public BaseResponse<PluginConfigVO> getConfig(@PathVariable Long pluginId,
                                                   @PathVariable String configKey) {
        PluginConfigVO config = pluginConfigService.getConfig(pluginId, configKey);
        return config != null ? ResultUtils.success(config) : ResultUtils.error(404, "配置不存在");
    }

    @GetMapping("/{pluginId}/{configKey}/bot/{botId}")
    @Operation(summary = "获取单个插件Bot专属配置", description = "根据插件ID、Bot ID和配置键获取配置（优先Bot专属配置）")
    public BaseResponse<PluginConfigVO> getConfigByBotId(
            @PathVariable Long pluginId,
            @PathVariable String configKey,
            @Parameter(description = "Bot ID") @PathVariable Long botId) {
        PluginConfigVO config = pluginConfigService.getConfig(pluginId, botId, configKey);
        return config != null ? ResultUtils.success(config) : ResultUtils.error(404, "配置不存在");
    }

    @PutMapping
    @Operation(summary = "更新插件配置", description = "更新插件配置信息")
    public BaseResponse<PluginConfigVO> updateConfig(@RequestBody UpdatePluginConfigRequest request) {
        PluginConfigVO config = pluginConfigService.updateConfig(request);
        return config != null ? ResultUtils.success(config) : ResultUtils.error(404, "配置不存在");
    }

    @PutMapping("/{pluginId}/{configKey}")
    @Operation(summary = "更新插件配置值", description = "根据插件ID和配置键更新默认配置值")
    public BaseResponse<Boolean> updateConfigValue(@PathVariable Long pluginId,
                                                    @PathVariable String configKey,
                                                    @RequestBody String configValue) {
        return ResultUtils.success(pluginConfigService.updateConfigValue(pluginId, configKey, configValue));
    }

    @PutMapping("/{pluginId}/{configKey}/bot/{botId}")
    @Operation(summary = "更新插件Bot专属配置值", description = "根据插件ID、Bot ID和配置键更新配置值（懒加载：若Bot专属配置不存在则创建）")
    public BaseResponse<Boolean> updateConfigValueByBotId(
            @PathVariable Long pluginId,
            @PathVariable String configKey,
            @Parameter(description = "Bot ID") @PathVariable Long botId,
            @RequestBody String configValue) {
        return ResultUtils.success(pluginConfigService.updateConfigValue(pluginId, botId, configKey, configValue));
    }
}
