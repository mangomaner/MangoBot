package io.github.mangomaner.mangobot.adapter.connect_controller.onebot.controller;

import io.github.mangomaner.mangobot.adapter.connect_controller.onebot.model.dto.CreateOneBotConfigRequest;
import io.github.mangomaner.mangobot.adapter.connect_controller.onebot.model.dto.UpdateOneBotConfigRequest;
import io.github.mangomaner.mangobot.adapter.connect_controller.onebot.model.vo.OneBotConfigVO;
import io.github.mangomaner.mangobot.adapter.connect_controller.onebot.service.OneBotConfigService;
import io.github.mangomaner.mangobot.system.common.BaseResponse;
import io.github.mangomaner.mangobot.system.common.ResultUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;

import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/connection/onebot")
@Tag(name = "OneBot 连接配置", description = "OneBot WebSocket 服务器配置管理接口")
public class OneBotConfigController {

    @Resource
    private OneBotConfigService oneBotConfigService;

    @GetMapping
    @Operation(summary = "获取所有配置", description = "获取所有 OneBot 配置列表")
    public BaseResponse<List<OneBotConfigVO>> listAll() {
        return ResultUtils.success(oneBotConfigService.listAll());
    }

    @GetMapping("/{id}")
    @Operation(summary = "获取配置详情", description = "根据ID获取 OneBot 配置详情")
    public BaseResponse<OneBotConfigVO> getById(@PathVariable Long id) {
        return ResultUtils.success(oneBotConfigService.getById(id));
    }

    @PostMapping
    @Operation(summary = "创建配置", description = "创建新的 OneBot 配置")
    public BaseResponse<Long> createConfig(@Valid @RequestBody CreateOneBotConfigRequest request) {
        return ResultUtils.success(oneBotConfigService.createConfig(request));
    }

    @PutMapping
    @Operation(summary = "更新配置", description = "更新 OneBot 配置信息")
    public BaseResponse<Boolean> updateConfig(@RequestBody UpdateOneBotConfigRequest request) {
        oneBotConfigService.updateConfig(request);
        return ResultUtils.success(true);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "删除配置", description = "删除指定的 OneBot 配置")
    public BaseResponse<Boolean> deleteConfig(@PathVariable Long id) {
        oneBotConfigService.deleteConfig(id);
        return ResultUtils.success(true);
    }

    @PostMapping("/{id}/start")
    @Operation(summary = "启动服务器", description = "启动指定配置的 WebSocket 服务器")
    public BaseResponse<Boolean> startServer(@PathVariable Long id) {
        oneBotConfigService.startServer(id);
        return ResultUtils.success(true);
    }

    @PostMapping("/{id}/stop")
    @Operation(summary = "停止服务器", description = "停止指定配置的 WebSocket 服务器")
    public BaseResponse<Boolean> stopServer(@PathVariable Long id) {
        oneBotConfigService.stopServer(id);
        return ResultUtils.success(true);
    }

    @PutMapping("/{id}/enabled")
    @Operation(summary = "设置启用状态", description = "设置配置的启用状态（启用会启动服务器，禁用会停止服务器）")
    public BaseResponse<Boolean> setEnabled(@PathVariable Long id, @RequestParam boolean enabled) {
        oneBotConfigService.setEnabled(id, enabled);
        return ResultUtils.success(true);
    }

    @GetMapping("/{id}/status")
    @Operation(summary = "获取服务器状态", description = "获取指定配置的 WebSocket 服务器运行状态")
    public BaseResponse<OneBotConfigVO> getServerStatus(@PathVariable Long id) {
        return ResultUtils.success(oneBotConfigService.getServerStatus(id));
    }
}
