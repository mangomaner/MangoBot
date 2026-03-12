package io.github.mangomaner.mangobot.manager;

import io.github.mangomaner.mangobot.api.MangoFileApi;
import io.github.mangomaner.mangobot.api.MangoGroupMessageApi;
import io.github.mangomaner.mangobot.api.MangoModelApi;
import io.github.mangomaner.mangobot.api.MangoOneBotApi;
import io.github.mangomaner.mangobot.api.MangoPrivateMessageApi;
import io.github.mangomaner.mangobot.configuration.service.ModelProvider;
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

    /**
     * 初始化静态 API 类
     */
    public void init() {
        try {
            initApi(MangoGroupMessageApi.class, "setService", GroupMessagesService.class, groupMessagesService);
            initApi(MangoPrivateMessageApi.class, "setService", PrivateMessagesService.class, privateMessagesService);
            initApi(MangoFileApi.class, "setService", BotFilesService.class, botFilesService);
            initApi(MangoOneBotApi.class, "setService", OneBotApiService.class, oneBotApiService);
            initApi(MangoModelApi.class, "setProvider", ModelProvider.class, modelProvider);
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
