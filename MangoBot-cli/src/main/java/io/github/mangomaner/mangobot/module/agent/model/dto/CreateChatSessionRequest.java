package io.github.mangomaner.mangobot.module.agent.model.dto;

import io.github.mangomaner.mangobot.module.agent.model.enums.SessionSource;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Builder;
import lombok.Data;

/**
 * 创建对话会话请求DTO
 */
@Data
@Schema(description = "创建对话会话请求")
@Builder
public class CreateChatSessionRequest {

    @NotBlank(message = "会话标题不能为空")
    @Schema(description = "会话标题", requiredMode = Schema.RequiredMode.REQUIRED)
    private String title;

    @Schema(description = "botId")
    private Long botId;

    @Schema(description = "群/私聊Id")
    private Long chatId;

    @Schema(description = "会话来源")
    private SessionSource source;
}
