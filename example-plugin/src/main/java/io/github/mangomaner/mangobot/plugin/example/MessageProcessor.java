package io.github.mangomaner.mangobot.plugin.example;

import io.github.mangomaner.mangobot.plugin.example.MessageService.MessageStatistics;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;

/**
 * 消息处理器
 *
 * <p>演示 Spring Component 的依赖注入和生命周期</p>
 */
@Slf4j
@Component
public class MessageProcessor {

    private final MessageService messageService;

    @Autowired
    public MessageProcessor(MessageService messageService) {
        this.messageService = messageService;
    }

    @PostConstruct
    public void init() {
        log.info("MessageProcessor 初始化完成");
    }

    @PreDestroy
    public void destroy() {
        log.info("MessageProcessor 销毁，最终统计: {}", messageService.getStatistics());
    }

    /**
     * 处理消息
     *
     * @param userId  用户 ID
     * @param message 消息内容
     * @return 处理结果
     */
    public String processMessage(long userId, String message) {
        // 记录消息
        messageService.recordMessage(userId, message);

        // 获取统计信息
        MessageStatistics stats = messageService.getStatistics();

        return String.format("消息已处理。您是第 %d 位用户，系统已处理 %d 条消息",
            stats.uniqueUsers(), stats.totalMessages());
    }

    /**
     * 获取用户统计
     *
     * @param userId 用户 ID
     * @return 用户统计信息
     */
    public String getUserStats(long userId) {
        int count = messageService.getUserMessageCount(userId);
        MessageStatistics stats = messageService.getStatistics();

        return String.format("您已发送 %d 条消息，系统总消息数: %d",
            count, stats.totalMessages());
    }
}
