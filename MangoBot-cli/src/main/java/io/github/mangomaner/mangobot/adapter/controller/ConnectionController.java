package io.github.mangomaner.mangobot.adapter.controller;

import io.github.mangomaner.mangobot.adapter.model.enums.PlatformType;
import io.github.mangomaner.mangobot.adapter.model.vo.PlatformOptionVO;
import io.github.mangomaner.mangobot.system.common.BaseResponse;
import io.github.mangomaner.mangobot.system.common.ResultUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/connection")
@Tag(name = "连接管理", description = "平台连接管理接口")
public class ConnectionController {

    @GetMapping("/platforms")
    @Operation(summary = "获取支持的平台列表", description = "获取所有支持的平台选项")
    public BaseResponse<List<PlatformOptionVO>> getPlatformOptions() {
        List<PlatformOptionVO> platforms = Arrays.stream(PlatformType.values())
                .map(type -> {
                    PlatformOptionVO vo = new PlatformOptionVO();
                    vo.setCode(type.getCode());
                    vo.setName(type.getName());
                    vo.setDescription(type.getDescription());
                    return vo;
                })
                .collect(Collectors.toList());
        return ResultUtils.success(platforms);
    }
}
