package io.github.mangomaner.mangobot.module.configuration.model.dto.model;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 测试模型请求
 */
@Data
@Schema(description = "测试模型请求")
public class TestModelRequest {

    @Schema(description = "测试消息", example = "你好")
    private String message;
}
