package io.github.mangomaner.mangobot.message_handler.response.tools;

import io.agentscope.core.tool.Tool;
import io.agentscope.core.tool.ToolParam;
import io.github.mangomaner.mangobot.annotation.MangoTool;
import io.github.mangomaner.mangobot.api.MangoOneBotApi;
import io.github.mangomaner.mangobot.api.context.ChatContext;
import io.github.mangomaner.mangobot.api.context.state.ToolExecuteState;
import io.github.mangomaner.mangobot.adapter.message_handler.onebot.outbound.build_sending_message.OneBotMessageBuilder;
import io.github.mangomaner.mangobot.adapter.message_handler.onebot.outbound.build_sending_message.OneBotSendingMessage;

@MangoTool(name = "GroupMessageSend", description = "发送群聊消息", category = "Group")
public class GroupMessageSendTool {
    @Tool(description = "发送文本消息")
    public String sendTextMessage(
            @ToolParam(name = "message", description = "要发送的消息内容，请遵循人设和回复规则")
            String message,
            ChatContext context
    ) {
        ToolExecuteState state = context.getToolExecuteState();
        if (state.getToolExecuteCount("sendTextMessage") > 0) {
            return "你已调用过该方法发送消息，请遵循规则，禁止重复调用，请结束回复或发送表情";
        }
        OneBotSendingMessage sendMessage = OneBotMessageBuilder.create().text(message).build();
        MangoOneBotApi.sendGroupMsg(Long.parseLong(context.getBotId()), Long.parseLong(context.getChatId()), sendMessage);
        state.addToolExecuteCount("sendTextMessage");
        return "发送成功";
    }
}
