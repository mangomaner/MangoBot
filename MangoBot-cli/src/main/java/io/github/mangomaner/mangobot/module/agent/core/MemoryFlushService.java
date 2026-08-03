package io.github.mangomaner.mangobot.module.agent.core;

import io.agentscope.core.agent.RuntimeContext;
import io.agentscope.core.message.Msg;
import io.agentscope.core.state.AgentState;
import io.agentscope.harness.agent.HarnessAgent;
import io.agentscope.harness.agent.memory.MemoryFlushManager;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import reactor.core.scheduler.Schedulers;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 长期记忆抽取异步化服务。
 *
 * <p>Harness 的 MemoryFlushMiddleware 会在流结束前同步执行记忆抽取（一次 LLM 调用，
 * 可能耗时十余秒），阻塞 [DONE] 导致前端长时间无法再次发送。构建 agent 时已将
 * FlushTrigger 设为 never() 关闭内联抽取，由本服务在每轮对话结束后手动触发
 * MemoryFlushManager.flushMemories，并在后台线程 fire-and-forget 执行。
 *
 * <p>内置节流随内联路径一并被关闭、无法复用，故这里用轻量节流替代：每个会话（userId）
 * 需要同时满足两个条件才触发抽取：
 * <ol>
 *   <li>距上次抽取已超过 flush-trigger-minutes 分钟；</li>
 *   <li>距上次抽取已积累至少 flush-trigger-messages 轮对话。</li>
 * </ol>
 * 仅按时间节流时，发送消息不频繁的用户会"每条消息都触发一次抽取"，而每次抽取都
 * 是对整个会话上下文的完整 LLM 调用，纯属浪费；加上对话次数维度后，稀疏对话不会
 * 每次都被抽取。时间戳与计数都初始化为"当前时刻 / 0"，因此新会话的首次对话不会
 * 触发抽取——修复了原实现时间戳默认 EPOCH(1970) 导致"新会话第一条消息必然抽取"
 * 的问题。
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class MemoryFlushService {

    private final AgentRuntimeProperties properties;

    /** 会话(userId) -> 抽取门槛状态 */
    private final Map<String, FlushState> states = new ConcurrentHashMap<>();

    /**
     * 检查节流后异步触发长期记忆抽取（不阻塞调用方）。
     */
    public void maybeFlushAsync(HarnessAgent agent, RuntimeContext context) {
        long gapMinutes = properties.getMemory().getFlushTriggerMinutes();
        int minMessages = properties.getMemory().getFlushTriggerMessages();
        String key = context.getUserId();

        if (gapMinutes <= 0) {
            // 0 = 每轮都抽取，不做任何限制
            flush(agent, context, key);
            return;
        }

        Instant now = Instant.now();
        FlushState state = states.computeIfAbsent(key, k -> new FlushState(now));
        boolean trigger;
        synchronized (state) {
            state.messages++;
            boolean timeElapsed = Duration.between(state.lastFlushAt, now).toMinutes() >= gapMinutes;
            boolean enoughMessages = minMessages <= 0 || state.messages >= minMessages;
            trigger = timeElapsed && enoughMessages;
            if (trigger) {
                state.lastFlushAt = now;
                state.messages = 0;
            }
        }
        if (trigger) {
            flush(agent, context, key);
        }
    }

    private void flush(HarnessAgent agent, RuntimeContext context, String key) {
        AgentState state = RuntimeContext.resolveAgentState(context, agent);
        if (state == null) {
            return;
        }
        List<Msg> msgs = state.getContext();
        if (msgs == null || msgs.isEmpty()) {
            return;
        }

        MemoryFlushManager manager = new MemoryFlushManager(agent.getWorkspaceManager(), agent.getModel());
        manager.flushMemories(context, msgs)
                .subscribeOn(Schedulers.boundedElastic())
                .subscribe(
                        null,
                        err -> log.warn("异步长期记忆抽取失败 [{}]: {}", key, err.getMessage()));
        log.info("已调度异步长期记忆抽取 [{}]", key);
    }

    /** 单个会话的抽取门槛状态（时间窗口 + 对话轮数） */
    private static final class FlushState {
        Instant lastFlushAt;
        int messages;

        FlushState(Instant lastFlushAt) {
            this.lastFlushAt = lastFlushAt;
        }
    }
}
