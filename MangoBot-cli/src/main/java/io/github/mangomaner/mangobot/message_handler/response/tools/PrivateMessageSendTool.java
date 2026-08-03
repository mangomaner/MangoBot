package io.github.mangomaner.mangobot.message_handler.response.tools;

import io.agentscope.core.tool.Tool;
import io.agentscope.core.tool.ToolParam;
import io.github.mangomaner.mangobot.adapter.onebot.handler.outbound.build_sending_message.OneBotMessageBuilder;
import io.github.mangomaner.mangobot.adapter.onebot.handler.outbound.build_sending_message.OneBotSendingMessage;
import io.github.mangomaner.mangobot.annotation.MangoTool;
import io.github.mangomaner.mangobot.api.MangoOneBotApi;
import io.github.mangomaner.mangobot.api.context.ChatContext;
import io.github.mangomaner.mangobot.api.context.state.ToolExecuteState;
import io.github.mangomaner.mangobot.module.agent.model.enums.SessionSource;

/**
 * 私聊消息发送工具
 */
@MangoTool(name = "PrivateMessageSend", description = "发送私聊消息", category = "Private")
public class PrivateMessageSendTool {

    @Tool(description = "发送文字消息（私聊）")
    public String sendPrivateTextMessage(
            @ToolParam(name = "message", description = "要发送的私聊消息内容，请遵循人设和回复规则")
            String message,
            ChatContext context) {
        ToolExecuteState state = context.getToolExecuteState();
        if (state.getToolExecuteCount("sendPrivateTextMessage") > 0) {
            return "你已调用过该方法发送消息，请结束回复";
        }
        if (context.getSource() != null && context.getSource() != SessionSource.PRIVATE) {
            return "该工具仅用于私聊会话，请改用群聊发送工具";
        }
        OneBotSendingMessage sendMessage = OneBotMessageBuilder.create().text(message).build();
        MangoOneBotApi.sendPrivateMsg(
                Long.parseLong(context.getBotId()),
                Long.parseLong(context.getChatId()),
                sendMessage);
        state.addToolExecuteCount("sendPrivateTextMessage");
        return "发送成功";
    }
}
