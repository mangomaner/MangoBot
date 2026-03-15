package io.github.mangomaner.mangobot.agent.model.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 创建对话会话请求DTO
 */
@Data
@Schema(description = "创建对话会话请求")
public class CreateChatSessionRequest {

    @NotBlank(message = "会话标题不能为空")
    @Schema(description = "会话标题", requiredMode = Schema.RequiredMode.REQUIRED)
    private String title;
}
