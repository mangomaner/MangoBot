package io.github.mangomaner.mangobot.handler;

import io.github.mangomaner.mangobot.module.agent.factory.AgentFactory;
import io.github.mangomaner.mangobot.annotation.PluginPriority;
import io.github.mangomaner.mangobot.annotation.messageHandler.MangoBotEventListener;
import io.github.mangomaner.mangobot.module.configuration.event.SystemConfigChangedEvent;
import io.github.mangomaner.mangobot.adapter.onebot.event.message.OneBotGroupMessageEvent;
import io.github.mangomaner.mangobot.adapter.onebot.event.message.OneBotPrivateMessageEvent;
import io.github.mangomaner.mangobot.module.message.groupMessage.service.GroupMessagesService;
import io.github.mangomaner.mangobot.module.message.privateMessage.service.PrivateMessagesService;
import io.github.mangomaner.mangobot.utils.MessageParser;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component
@Slf4j
@MangoBotEventListener
public class MessageStoreHandler {

    @Resource
    private GroupMessagesService groupMessagesService;

    @Resource
    private PrivateMessagesService privateMessagesService;

    @Resource
    private MessageParser messageParser;

    @Resource
    private AgentFactory agentFactory;

    @MangoBotEventListener
    @PluginPriority(-1)
    public boolean onGroupMessage(OneBotGroupMessageEvent event) {
        log.info("收到消息: " + event.getMessage());
        String parseMessage = messageParser.parseMessage(event.getMessage(), event.getSelfId());
        event.setParsedMessage(parseMessage);
        groupMessagesService.addGroupMessage(event);
        return true;
    }

    @MangoBotEventListener
    @PluginPriority(-1)
    public boolean onPrivateMessage(OneBotPrivateMessageEvent event) {
        log.info("收到消息: " + event.getMessage());
        String parseMessage = messageParser.parseMessage(event.getMessage(), event.getSelfId());
        event.setParsedMessage(parseMessage);
        privateMessagesService.addPrivateMessage(event);
        return true;
    }

    @MangoBotEventListener
    public boolean onConfigChange(SystemConfigChangedEvent event) {    
        log.info("收到配置变更通知: key={}, value={}", event.getConfigKey(), event.getNewValue());
        return true;
    }

}
