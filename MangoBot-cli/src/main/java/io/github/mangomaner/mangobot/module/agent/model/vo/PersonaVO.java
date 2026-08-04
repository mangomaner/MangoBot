package io.github.mangomaner.mangobot.module.agent.model.vo;

import io.github.mangomaner.mangobot.module.agent.model.enums.SessionSource;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

/**
 * 会话人格提示词视图对象
 */
@Data
@Builder
@Schema(description = "会话人格提示词信息")
public class PersonaVO {

    @Schema(description = "Bot ID")
    private String botId;

    @Schema(description = "群/私聊ID")
    private String chatId;

    @Schema(description = "会话来源")
    private SessionSource source;

    @Schema(description = "来源默认人格提示词（AGENTS_GROUP/AGENTS_PRIVATE 或 Web AGENTS.md）")
    private String defaultPrompt;

    @Schema(description = "已定制的提示词（null 表示未定制）")
    private String customPrompt;

    @Schema(description = "是否已定制提示词")
    private Boolean hasCustomPrompt;
}
