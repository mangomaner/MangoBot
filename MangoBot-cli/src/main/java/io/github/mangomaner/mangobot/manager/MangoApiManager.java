package io.github.mangomaner.mangobot.manager;

import io.github.mangomaner.mangobot.agent.capability.tool.ToolRegistrationService;
import io.github.mangomaner.mangobot.agent.factory.AgentFactory;
import io.github.mangomaner.mangobot.agent.service.ChatService;
import io.github.mangomaner.mangobot.agent.service.ChatSessionService;
import io.github.mangomaner.mangobot.api.*;
import io.github.mangomaner.mangobot.configuration.core.ModelProvider;
import io.github.mangomaner.mangobot.configuration.service.PluginConfigService;
import io.github.mangomaner.mangobot.configuration.service.SystemConfigService;
import io.github.mangomaner.mangobot.service.BotFilesService;
import io.github.mangomaner.mangobot.service.GroupMessagesService;
import io.github.mangomaner.mangobot.service.OneBotApiService;
import io.github.mangomaner.mangobot.service.PrivateMessagesService;
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
    private PluginConfigService pluginConfigService;

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
            initApi(MangoModelApi.class, "setProvider", ModelProvider.class, modelProvider);
            initApi(MangoToolApi.class, "setService", ToolRegistrationService.class, toolRegistrationService);
            initApi(MangoAgentApi.class, "setChatSessionService", ChatSessionService.class, chatSessionService);
            initApi(MangoAgentApi.class, "setAgentFactory", AgentFactory.class, agentFactory);
            initApi(MangoAgentApi.class, "setChatService", ChatService.class, chatService);
            initApi(MangoConfigApi.class, "setSystemConfigService", SystemConfigService.class, systemConfigService);
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
