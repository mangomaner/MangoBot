package io.github.mangomaner.mangobot.module.agent.core;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * Agent 运行时公共配置（所有 Bot 共用）
 *
 * <pre>
 * mangobot:
 *   agent:
 *     compaction:
 *       trigger-tokens: 60000   # 上下文估算超过 60K tokens 时触发压缩
 *       keep-tokens: 30000      # 压缩后保留尾部约 30K tokens
 *       flush-before-compact: true
 *     memory:
 *       flush-trigger-minutes: 10  # 长期记忆后台异步抽取的最小间隔（分钟）
 *       flush-trigger-messages: 5  # 距上次抽取至少积累的对话轮数（0 表示仅按时间触发）
 * </pre>
 */
@Data
@Component
@ConfigurationProperties(prefix = "mangobot.agent")
public class AgentRuntimeProperties {

    /** 上下文压缩配置 */
    private Compaction compaction = new Compaction();

    /** 长期记忆配置 */
    private Memory memory = new Memory();

    @Data
    public static class Compaction {
        /** 上下文估算超过该 token 数时触发压缩 */
        private int triggerTokens = 60000;
        /** 压缩后保留尾部 token 预算 */
        private int keepTokens = 30000;
        /** 压缩前先把新事实写入 memory/ 日流水账 */
        private boolean flushBeforeCompact = true;
    }

    @Data
    public static class Memory {
        /** 长期记忆后台异步抽取的最小间隔（分钟），0 表示每轮对话都抽取 */
        private long flushTriggerMinutes = 10;
        /** 距上次抽取至少积累的对话轮数后才触发（0 表示仅按时间触发） */
        private int flushTriggerMessages = 5;
    }
}
