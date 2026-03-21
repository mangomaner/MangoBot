package io.github.mangomaner.mangobot.api.context;

import io.github.mangomaner.mangobot.api.context.state.ToolExecuteState;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ChatContext {
    private Integer sessionId;
    private String botId;
    private String chatId;
    private ToolExecuteState toolExecuteState;
}
