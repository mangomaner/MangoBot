package io.github.mangomaner.mangobot.api.context;

import io.github.mangomaner.mangobot.api.context.state.ToolExecuteState;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ChatContext {
    private Integer sessionId;
    private Long botId;
    private Long chatId;
    private ToolExecuteState toolExecuteState;
}
