package io.github.mangomaner.mangobot.agent.model.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 流式对话请求DTO
 */
@Data
@Schema(description = "流式对话请求")
public class StreamChatRequest {

    @NotNull(message = "会话ID不能为空")
    @Schema(description = "对话会话ID", requiredMode = Schema.RequiredMode.REQUIRED)
    private Integer sessionId;

    @NotBlank(message = "消息内容不能为空")
    @Schema(description = "用户消息内容", requiredMode = Schema.RequiredMode.REQUIRED)
    private String message;
}
