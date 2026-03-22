package io.github.mangomaner.mangobot.module.configuration.controller;

import io.github.mangomaner.mangobot.system.common.BaseResponse;
import io.github.mangomaner.mangobot.system.common.ResultUtils;
import io.github.mangomaner.mangobot.module.configuration.model.dto.bot.CreateBotConfigRequest;
import io.github.mangomaner.mangobot.module.configuration.model.dto.bot.UpdateBotConfigRequest;
import io.github.mangomaner.mangobot.module.configuration.model.vo.BotConfigVO;
import io.github.mangomaner.mangobot.module.configuration.service.BotConfigService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;

import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/configuration/bot")
@Tag(name = "Bot 配置管理", description = "Bot 配置的增删改查接口")
public class BotConfigController {

    @Resource
    private BotConfigService botConfigService;

    @GetMapping
    @Operation(summary = "获取所有 Bot 配置", description = "获取所有默认 Bot 配置列表（bot_id 为 null）")
    public BaseResponse<List<BotConfigVO>> getAllConfigs() {
        return ResultUtils.success(botConfigService.getAllConfigs());
    }

    @GetMapping("/bot/{botId}")
    @Operation(summary = "根据 Bot ID 获取配置", description = "获取指定 Bot 的配置（懒加载模式：返回默认配置和 Bot 专属配置，优先展示 Bot 专属配置）")
    public BaseResponse<List<BotConfigVO>> getConfigsByBotId(
            @Parameter(description = "Bot ID") @PathVariable String botId) {
        return ResultUtils.success(botConfigService.getConfigsByBotId(botId));
    }

    @GetMapping("/category/{category}")
    @Operation(summary = "按分类获取配置", description = "根据分类获取默认 Bot 配置列表")
    public BaseResponse<List<BotConfigVO>> getConfigsByCategory(@PathVariable String category) {
        return ResultUtils.success(botConfigService.getConfigsByCategory(category));
    }

    @GetMapping("/{configKey}")
    @Operation(summary = "根据 Key 获取配置", description = "根据配置键获取默认 Bot 配置详情")
    public BaseResponse<BotConfigVO> getConfigByKey(@PathVariable String configKey) {
        BotConfigVO config = botConfigService.getConfigByKey(configKey);
        return config != null ? ResultUtils.success(config) : ResultUtils.error(404, "配置不存在");
    }

    @GetMapping("/{configKey}/bot/{botId}")
    @Operation(summary = "根据 Key 和 Bot ID 获取配置", description = "根据配置键和 Bot ID 获取配置（优先返回 Bot 专属配置）")
    public BaseResponse<BotConfigVO> getConfigByKeyAndBotId(
            @PathVariable String configKey,
            @Parameter(description = "Bot ID") @PathVariable String botId) {
        BotConfigVO config = botConfigService.getConfigByKeyAndBotId(configKey, botId);
        return config != null ? ResultUtils.success(config) : ResultUtils.error(404, "配置不存在");
    }

    @PostMapping
    @Operation(summary = "创建 Bot 配置", description = "创建新的 Bot 配置")
    public BaseResponse<BotConfigVO> createConfig(@Valid @RequestBody CreateBotConfigRequest request) {
        return ResultUtils.success(botConfigService.createConfig(request));
    }

    @PutMapping
    @Operation(summary = "更新 Bot 配置", description = "更新 Bot 配置信息")
    public BaseResponse<BotConfigVO> updateConfig(@RequestBody UpdateBotConfigRequest request) {
        BotConfigVO config = botConfigService.updateConfig(request);
        return config != null ? ResultUtils.success(config) : ResultUtils.error(404, "配置不存在");
    }

    @PutMapping("/{configKey}")
    @Operation(summary = "更新配置值", description = "根据配置键更新默认配置值")
    public BaseResponse<Boolean> updateConfigValue(@PathVariable String configKey,
                                                    @RequestBody String configValue) {
        return ResultUtils.success(botConfigService.updateConfigValue(configKey, configValue));
    }

    @PutMapping("/{configKey}/bot/{botId}")
    @Operation(summary = "更新 Bot 专属配置值", description = "根据配置键和 Bot ID 更新配置值（懒加载：若 Bot 专属配置不存在则创建）")
    public BaseResponse<Boolean> updateConfigValueByBotId(
            @PathVariable String configKey,
            @Parameter(description = "Bot ID") @PathVariable String botId,
            @RequestBody String configValue) {
        return ResultUtils.success(botConfigService.updateConfigValue(configKey, botId, configValue));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "删除 Bot 配置", description = "删除指定的 Bot 配置")
    public BaseResponse<Boolean> deleteConfig(@PathVariable Long id) {
        return ResultUtils.success(botConfigService.deleteConfig(id));
    }
}
