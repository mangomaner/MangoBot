package io.github.mangomaner.mangobot.plugin.example;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 消息统计服务
 *
 * <p>演示 Spring Service 在插件中的使用</p>
 */
@Slf4j
@Service
public class MessageService {

    private final AtomicInteger messageCount = new AtomicInteger(0);
    private final ConcurrentHashMap<Long, Integer> userMessageCount = new ConcurrentHashMap<>();
    private final LocalDateTime startTime = LocalDateTime.now();

    /**
     * 记录消息
     *
     * @param userId  用户 ID
     * @param message 消息内容
     */
    public void recordMessage(long userId, String message) {
        int total = messageCount.incrementAndGet();
        int userCount = userMessageCount.merge(userId, 1, Integer::sum);
        log.debug("消息统计 - 总消息数: {}, 用户 {} 的消息数: {}", total, userId, userCount);
    }

    /**
     * 获取消息统计信息
     *
     * @return 统计信息
     */
    public MessageStatistics getStatistics() {
        return new MessageStatistics(
            messageCount.get(),
            userMessageCount.size(),
            startTime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))
        );
    }

    /**
     * 获取指定用户的消息数
     *
     * @param userId 用户 ID
     * @return 消息数
     */
    public int getUserMessageCount(long userId) {
        return userMessageCount.getOrDefault(userId, 0);
    }

    /**
     * 重置统计
     */
    public void reset() {
        messageCount.set(0);
        userMessageCount.clear();
        log.info("消息统计已重置");
    }

    /**
     * 消息统计信息
     */
    public record MessageStatistics(int totalMessages, int uniqueUsers, String startTime) {
    }
}
