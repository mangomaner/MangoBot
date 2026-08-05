package io.github.mangomaner.mangobot.module.agent.model.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 开启/关闭会话调试记录请求
 */
@Data
@Schema(description = "开启/关闭会话调试记录请求")
public class DebugRecordRequest {

    @Schema(description = "会话ID")
    private Integer sessionId;

    @Schema(description = "是否开启记录")
    private Boolean enabled;
}
