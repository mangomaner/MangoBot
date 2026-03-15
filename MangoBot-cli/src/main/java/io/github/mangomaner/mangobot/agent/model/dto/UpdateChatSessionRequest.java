package io.github.mangomaner.mangobot.agent.model.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 更新对话会话请求DTO
 */
@Data
@Schema(description = "更新对话会话请求")
public class UpdateChatSessionRequest {

    @NotBlank(message = "会话标题不能为空")
    @Schema(description = "会话标题", requiredMode = Schema.RequiredMode.REQUIRED)
    private String title;
}
