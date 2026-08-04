package io.github.mangomaner.mangobot.module.agent.model.dto;

import io.github.mangomaner.mangobot.module.agent.model.enums.SessionSource;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 设置会话定制人格提示词请求DTO
 */
@Data
@Schema(description = "设置会话定制人格提示词请求")
public class SetPersonaRequest {

    @Schema(description = "Bot ID")
    private String botId;

    @Schema(description = "群/私聊ID")
    private String chatId;

    @Schema(description = "会话来源")
    private SessionSource source;

    @Schema(description = "定制人格提示词（null 或空白表示清除定制）")
    private String customPrompt;
}
