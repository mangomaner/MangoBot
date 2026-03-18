package io.github.mangomaner.mangobot.agent.tools;

import io.agentscope.core.tool.Tool;
import io.agentscope.core.tool.ToolParam;
import io.github.mangomaner.mangobot.annotation.MangoTool;
import io.github.mangomaner.mangobot.api.MangoOneBotApi;
import io.github.mangomaner.mangobot.api.context.ChatContext;
import io.github.mangomaner.mangobot.model.onebot.MessageBuilder;
import io.github.mangomaner.mangobot.model.onebot.SendMessage;

@MangoTool(name = "GroupMessageSend", description = "发送群聊消息", category = "Group")
public class GroupMessageSendTool {
    @Tool(description = "发送文本消息")
    public String sendTextMessage(
            @ToolParam(name = "message", description = "要发送的消息内容，请遵循人设和回复规则")
            String message,
            ChatContext context
    ) {
        SendMessage sendMessage = MessageBuilder.create().text(message).build();
        MangoOneBotApi.sendGroupMsg(context.getBotId(), context.getChatId(), sendMessage);
        return "发送成功";
    }
}
