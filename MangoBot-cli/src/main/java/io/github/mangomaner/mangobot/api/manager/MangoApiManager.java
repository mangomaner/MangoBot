package io.github.mangomaner.mangobot.api.manager;

import io.github.mangomaner.mangobot.module.agent.capability.tool.ToolRegistrationService;
import io.github.mangomaner.mangobot.module.agent.factory.AgentFactory;
import io.github.mangomaner.mangobot.module.agent.workspace.ChatService;
import io.github.mangomaner.mangobot.module.agent.service.ChatSessionService;
import io.github.mangomaner.mangobot.api.*;
import io.github.mangomaner.mangobot.module.configuration.core.ModelProvider;
import io.github.mangomaner.mangobot.module.configuration.service.BotConfigService;
import io.github.mangomaner.mangobot.module.configuration.service.PluginConfigService;
import io.github.mangomaner.mangobot.module.configuration.service.SystemConfigService;
import io.github.mangomaner.mangobot.module.file.service.BotFilesService;
import io.github.mangomaner.mangobot.module.message.groupMessage.service.GroupMessagesService;
import io.github.mangomaner.mangobot.adapter.onebot.handler.outbound.send.OneBotApiService;
import io.github.mangomaner.mangobot.module.message.privateMessage.service.PrivateMessagesService;
import io.github.mangomaner.mangobot.adapter.onebot.utils.MessageParser;
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
    private MessageParser messageParser;

    /**
     * 初始化静态 API 类
     */
    public void init() {
        try {
            initApi(MangoGroupMessageApi.class, "setService", GroupMessagesService.class, groupMessagesService);
            initApi(MangoPrivateMessageApi.class, "setService", PrivateMessagesService.class, privateMessagesService);
            initApi(MangoFileApi.class, "setService", BotFilesService.class, botFilesService);
            initApi(MangoOneBotApi.class, "setService", OneBotApiService.class, oneBotApiService);
            initApi(MangoOneBotApi.class, "setGroupMessagesService", GroupMessagesService.class, groupMessagesService);
            initApi(MangoOneBotApi.class, "setPrivateMessagesService", PrivateMessagesService.class, privateMessagesService);
            initApi(MangoOneBotApi.class, "setMessageParser", MessageParser.class, messageParser);
            initApi(MangoModelApi.class, "setProvider", ModelProvider.class, modelProvider);
            initApi(MangoToolApi.class, "setService", ToolRegistrationService.class, toolRegistrationService);
            initApi(MangoAgentApi.class, "setChatSessionService", ChatSessionService.class, chatSessionService);
            initApi(MangoAgentApi.class, "setAgentFactory", AgentFactory.class, agentFactory);
            initApi(MangoAgentApi.class, "setChatService", ChatService.class, chatService);
            initApi(MangoConfigApi.class, "setSystemConfigService", SystemConfigService.class, systemConfigService);
            initApi(MangoConfigApi.class, "setBotConfigService", BotConfigService.class, botConfigService);
            initApi(MangoConfigApi.class, "setPluginConfigService", PluginConfigService.class, pluginConfigService);
        } catch (Exception e) {
            throw new RuntimeException("Failed to initialize Mango APIs", e);
        }
    }

    private void initApi(Class<?> apiClass, String methodName, Class<?> serviceType, Object serviceInstance) throws Exception {
        java.lang.reflect.Method method = apiClass.getDeclaredMethod(methodName, serviceType);
        method.setAccessible(true);
        method.invoke(null, serviceInstance);
    }
}
