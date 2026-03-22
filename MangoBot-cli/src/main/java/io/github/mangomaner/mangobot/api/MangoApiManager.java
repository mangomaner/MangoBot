package io.github.mangomaner.mangobot.api;

import io.github.mangomaner.mangobot.module.agent.capability.tool.ToolRegistrationService;
import io.github.mangomaner.mangobot.module.agent.factory.AgentFactory;
import io.github.mangomaner.mangobot.module.agent.workspace.ChatService;
import io.github.mangomaner.mangobot.module.agent.service.ChatSessionService;
import io.github.mangomaner.mangobot.module.configuration.core.ModelProvider;
import io.github.mangomaner.mangobot.module.configuration.service.BotConfigService;
import io.github.mangomaner.mangobot.module.configuration.service.PluginConfigService;
import io.github.mangomaner.mangobot.module.configuration.service.SystemConfigService;
import io.github.mangomaner.mangobot.module.file.service.BotFilesService;
import io.github.mangomaner.mangobot.module.message.groupMessage.service.GroupMessagesService;
import io.github.mangomaner.mangobot.adapter.onebot.handler.outbound.send.OneBotApiService;
import io.github.mangomaner.mangobot.module.message.privateMessage.service.PrivateMessagesService;
import io.github.mangomaner.mangobot.adapter.onebot.utils.OneBotMessageParser;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Component;

/**
 * 负责初始化静态 API 类的管理器
 */
@Component
public class MangoApiManager {

    @Resource
    private GroupMessagesService groupMessagesService;

    @Resource
    private PrivateMessagesService privateMessagesService;

    @Resource
    private BotFilesService botFilesService;

    @Resource
    private OneBotApiService oneBotApiService;

    @Resource
    private ModelProvider modelProvider;

    @Resource
    private ToolRegistrationService toolRegistrationService;

    @Resource
    private ChatSessionService chatSessionService;

    @Resource
    private AgentFactory agentFactory;

    @Resource
    private ChatService chatService;

    @Resource
    private SystemConfigService systemConfigService;

    @Resource
    private BotConfigService botConfigService;

    @Resource
    private PluginConfigService pluginConfigService;

    @Resource
    private OneBotMessageParser messageParser;

    /**
     * 初始化静态 API 类
     */
    public void init() {
        MangoGroupMessageApi.setService(groupMessagesService);
        MangoPrivateMessageApi.setService(privateMessagesService);
        MangoFileApi.setService(botFilesService);
        
        MangoOneBotApi.setService(oneBotApiService);
        MangoOneBotApi.setGroupMessagesService(groupMessagesService);
        MangoOneBotApi.setPrivateMessagesService(privateMessagesService);
        MangoOneBotApi.setMessageParser(messageParser);
        
        MangoModelApi.setProvider(modelProvider);
        MangoToolApi.setService(toolRegistrationService);
        
        MangoAgentApi.setChatSessionService(chatSessionService);
        MangoAgentApi.setAgentFactory(agentFactory);
        MangoAgentApi.setChatService(chatService);
        
        MangoConfigApi.setSystemConfigService(systemConfigService);
        MangoConfigApi.setBotConfigService(botConfigService);
        MangoConfigApi.setPluginConfigService(pluginConfigService);
    }
}
