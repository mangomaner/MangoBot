package io.github.mangomaner.mangobot.module.agent.chat;

import io.agentscope.core.agent.RuntimeContext;
import io.agentscope.core.event.ModelCallEndEvent;
import io.agentscope.core.event.ToolCallStartEvent;
import io.agentscope.core.message.UserMessage;
import io.agentscope.harness.agent.HarnessAgent;
import io.github.mangomaner.mangobot.adapter.onebot.handler.outbound.build_sending_message.OneBotMessageBuilder;
import io.github.mangomaner.mangobot.adapter.onebot.handler.outbound.build_sending_message.OneBotSendingMessage;
import io.github.mangomaner.mangobot.api.MangoOneBotApi;
import io.github.mangomaner.mangobot.api.context.ChatContext;
import io.github.mangomaner.mangobot.api.context.state.ToolExecuteState;
import io.github.mangomaner.mangobot.module.agent.core.AgentPathKeys;
import io.github.mangomaner.mangobot.module.agent.core.AgentRuntimeRegistry;
import io.github.mangomaner.mangobot.module.agent.core.MemoryFlushService;
import io.github.mangomaner.mangobot.module.agent.model.dto.ChatMessageWebRequest;
import io.github.mangomaner.mangobot.module.agent.model.enums.SessionSource;
import io.github.mangomaner.mangobot.module.agent.model.vo.ChatSessionVO;
import io.github.mangomaner.mangobot.module.agent.model.vo.TokenUsageVO;
import io.github.mangomaner.mangobot.module.agent.service.ChatMessageWebService;
import io.github.mangomaner.mangobot.module.agent.service.ChatSessionService;
import io.github.mangomaner.mangobot.module.agent.service.SessionPersonaService;
import io.github.mangomaner.mangobot.utils.TokenUsageUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.util.Locale;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

/**
 * 统一会话入口：负责
 * <ol>
 *   <li>按 sessionId 解析 Bot 单例 HarnessAgent（AgentRuntimeRegistry）</li>
 *   <li>组装文件系统安全的 RuntimeContext（userId/sessionId）并注入 ChatContext（含 source）</li>
 *   <li>订阅 streamEvents，经 ChatEventMapper 转 SSE 文本</li>
 *   <li>消息投影双写（chat_messages 统一消息表）</li>
 *   <li>同一会话运行中收到新消息时先 interrupt 旧调用，再立即发起新一轮（运行中打断 + 合并续答）</li>
 *   <li>私聊强制回复：一轮未调用发送工具则自动重试一次（最多一次），确保至少回复一条</li>
 * </ol>
 *
 * <p>消息不再使用 XML 包装（&lt;UserMessage messageId=...&gt;），直接传原始文本；
 * 发送者身份通过 UserMessage 的 name 字段带给模型，会话历史由 AgentState 自动承载。
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ChatOrchestrator {

    private final AgentRuntimeRegistry agentRuntimeRegistry;
    private final ChatSessionService chatSessionService;
    private final ChatMessageWebService chatMessageWebService;
    private final ChatEventMapper chatEventMapper;
    private final MemoryFlushService memoryFlushService;
    private final SessionPersonaService sessionPersonaService;

    /** sessionId -> 运行代数（0 表示空闲；>0 表示有 in-flight 调用） */
    private final ConcurrentHashMap<Integer, AtomicInteger> runningGenerations = new ConcurrentHashMap<>();

    private static final String ROLE_USER = "user";
    /** 私聊强制回复：首轮之外最多再重试 1 次 */
    private static final int MAX_REPLY_RETRY = 1;

    public Flux<String> streamChat(Integer sessionId, String message) {
        return streamChat(sessionId, message, null);
    }

    /**
     * 执行流式对话。
     *
     * @param sessionId  会话 ID
     * @param message    用户消息原文（不做 XML 包装）
     * @param senderName 发送者标识（群聊/私聊为发送者 QQ 号），作为 UserMessage.name
     */
    public Flux<String> streamChat(Integer sessionId, String message, String senderName) {
        if (sessionId == null) {
            return Flux.just("<Error>会话ID不能为空</Error>\n[DONE]");
        }
        if (!StringUtils.hasText(message)) {
            return Flux.just("<Error>消息内容不能为空</Error>\n");
        }

        ChatSessionVO session = chatSessionService.getSessionById(sessionId);
        // 群聊/私聊：发消息前物化生效人格（定制或来源默认）到会话级 AGENTS.md
        if (session.getSource() == SessionSource.GROUP || session.getSource() == SessionSource.PRIVATE) {
            sessionPersonaService.materializeSessionPersona(session);
        }
        HarnessAgent agent = agentRuntimeRegistry.getOrCreate(session.getBotId(), session.getSource());
        String userId = SessionKeys.userId(session);
        String sessionKey = SessionKeys.sessionId(session, sessionId);

        AtomicInteger generation = runningGenerations.computeIfAbsent(sessionId, k -> new AtomicInteger(0));
        if (generation.get() > 0) {
            // 运行中打断：让旧调用尽快在下一个检查点终止（AgentState 自动保存）
            try {
                agent.getDelegate().interrupt(userId, sessionKey);
                log.info("Interrupted in-flight call for session: {}", sessionId);
            } catch (Exception e) {
                log.warn("Failed to interrupt in-flight call for session: {}", sessionId, e);
            }
        }

        persistUserMessage(sessionId, message);

        RuntimeContext context = RuntimeContext.builder()
                .userId(userId)
                .sessionId(sessionKey)
                .put(ChatContext.class, ChatContext.builder()
                        .sessionId(sessionId)
                        .botId(session.getBotId())
                        .chatId(session.getChatId())
                        .source(session.getSource())
                        .toolExecuteState(new ToolExecuteState())
                        .build())
                .build();

        int myGeneration = generation.incrementAndGet();

        return runTurns(session, agent, context, message, senderName, 0, generation, myGeneration)
                .subscribeOn(Schedulers.boundedElastic())
                .doFinally(signalType -> {
                    // 只有当前这一代还在运行才清理，避免旧调用的收尾清掉新调用的状态
                    if (generation.get() == myGeneration) {
                        generation.set(0);
                        runningGenerations.remove(sessionId, generation);
                    }
                });
    }

    private Flux<String> runTurns(ChatSessionVO session, HarnessAgent agent, RuntimeContext context,
                                  String message, String senderName, int attempt,
                                  AtomicInteger generation, int myGeneration) {
        AtomicBoolean sendToolInvoked = new AtomicBoolean(false);
        StringBuilder assistantBuffer = new StringBuilder();
        // 一轮 ReAct 可能有多次模型调用：累计全部调用的用量，整轮结束时输出一次总和
        AtomicReference<TokenUsageVO> totalUsage = new AtomicReference<>();

        Flux<String> turn = agent.streamEvents(
                        new UserMessage(
                                senderName != null && !senderName.isBlank() ? senderName : ROLE_USER,
                                message),
                        context)
                .flatMap(event -> {
                    if (event instanceof ToolCallStartEvent start && isSendTool(start.getToolCallName())) {
                        sendToolInvoked.set(true);
                    }
                    if (event instanceof ModelCallEndEvent end) {
                        TokenUsageVO vo = TokenUsageUtils.extractFromChatUsage(end.getUsage());
                        if (vo != null) {
                            TokenUsageVO current = totalUsage.get();
                            if (current == null) {
                                totalUsage.set(vo);
                            } else {
                                totalUsage.set(TokenUsageVO.builder()
                                        .inputTokens(safe(current.getInputTokens()) + safe(vo.getInputTokens()))
                                        .outputTokens(safe(current.getOutputTokens()) + safe(vo.getOutputTokens()))
                                        .totalTokens(safe(current.getTotalTokens()) + safe(vo.getTotalTokens()))
                                        .time((current.getTime() != null ? current.getTime() : 0)
                                                + (vo.getTime() != null ? vo.getTime() : 0))
                                        .cachedTokens(safe(current.getCachedTokens()) + safe(vo.getCachedTokens()))
                                        .build());
                            }
                        }
                    }
                    // map 不允许返回 null，用 justOrEmpty 过滤掉无输出的事件
                    return Mono.justOrEmpty(chatEventMapper.map(event));
                })
                .doOnNext(assistantBuffer::append)
                .onErrorResume(error -> {
                    log.error("Stream error for session {}: {}", session.getId(), error.getMessage(), error);
                    return Flux.just("\n<Error>" + error.getMessage() + "</Error>\n");
                });

        return turn.concatWith(Flux.defer(() -> {
            // Token 用量：整轮结束时输出一次，并写入持久化内容
            Flux<String> post = Flux.empty();
            TokenUsageVO usage = totalUsage.get();
            if (usage != null) {
                String usageChunk = chatEventMapper.serializeTokenUsage(usage);
                if (usageChunk != null) {
                    assistantBuffer.append(usageChunk);
                    post = post.concatWith(Flux.just(usageChunk));
                }
            }

            persistAssistantMessage(session.getId(), assistantBuffer.toString());

            boolean stillCurrent = generation.get() == myGeneration;
            boolean sendInvoked = sendToolInvoked.get();
            String plainText = extractPlainText(assistantBuffer.toString());

            // 模型直接输出了文本但没有调用发送工具：自动代为发送，确保回复真正送达
            if (stillCurrent && !sendInvoked && StringUtils.hasText(plainText)
                    && session.getSource() != SessionSource.WEB) {
                try {
                    sendReply(session, plainText);
                    log.info("Auto-sent reply for session {}: {}", session.getId(), plainText);
                } catch (Exception e) {
                    log.error("Failed to auto-send reply for session: {}", session.getId(), e);
                }
            }

            // 私聊且既没调用发送工具也没生成文本：重试一次（若已被更新的调用打断则不再重试）
            boolean needRetry = session.getSource() == SessionSource.PRIVATE
                    && !sendInvoked
                    && !StringUtils.hasText(plainText)
                    && attempt < MAX_REPLY_RETRY
                    && stillCurrent;
            if (needRetry) {
                log.info("Private session {} produced no reply, retrying once", session.getId());
                String reminder = "（系统提醒）你还没有生成任何回复内容。私聊中必须回复，请立即调用发送工具发送一条消息。";
                return post.concatWith(Flux.just("\n<Reminder>私聊必须回复，正在重试...</Reminder>\n"))
                        .concatWith(runTurns(session, agent, context, reminder, null, attempt + 1,
                                generation, myGeneration));
            }
            return post;
        })).concatWith(Flux.defer(() -> {
            // 长期记忆抽取移到后台：不阻塞 [DONE]，前端可立即继续发送
            memoryFlushService.maybeFlushAsync(agent, context);
            return Flux.just("[DONE]");
        }));
    }

    private static boolean isSendTool(String toolName) {
        return toolName != null && toolName.toLowerCase(Locale.ROOT).startsWith("send");
    }

    /**
     * 从累积的 SSE 内容中提取纯文本（去掉各类标签）
     */
    private static String extractPlainText(String raw) {
        String text = raw
                .replaceAll("(?s)<Thinking>.*?</Thinking>", "")
                .replaceAll("(?s)<FunctionCall>.*?</FunctionCall>", "")
                .replaceAll("(?s)<FunctionCallResult>.*?</FunctionCallResult>", "")
                .replaceAll("(?s)<TokenUsage>.*?</TokenUsage>", "")
                .replaceAll("(?s)<Error>.*?</Error>", "")
                .replaceAll("(?s)<Reminder>.*?</Reminder>", "")
                .replace("\u0000", "\n")
                .trim();
        return text;
    }

    /**
     * 代发回复：模型输出了文本但没调用发送工具时，直接发送
     */
    private void sendReply(ChatSessionVO session, String text) {
        if (session.getSource() != SessionSource.GROUP && session.getSource() != SessionSource.PRIVATE) {
            return;
        }
        if (!isNumeric(session.getBotId()) || !isNumeric(session.getChatId())) {
            log.warn("Cannot auto-send reply: invalid target for session {} (botId={}, chatId={})",
                    session.getId(), session.getBotId(), session.getChatId());
            return;
        }
        OneBotSendingMessage message = OneBotMessageBuilder.create().text(text).build();
        long botId = Long.parseLong(session.getBotId());
        long targetId = Long.parseLong(session.getChatId());
        if (session.getSource() == SessionSource.GROUP) {
            MangoOneBotApi.sendGroupMsg(botId, targetId, message);
        } else {
            MangoOneBotApi.sendPrivateMsg(botId, targetId, message);
        }
    }

    private static boolean isNumeric(String value) {
        return value != null && value.matches("\\d+");
    }

    private static long safe(Long value) {
        return value != null ? value : 0L;
    }

    /** 该会话当前是否有 in-flight 调用 */
    public boolean isRunning(Integer sessionId) {
        AtomicInteger generation = runningGenerations.get(sessionId);
        return generation != null && generation.get() > 0;
    }

    /** 中断指定会话的 in-flight 调用（无消息注入，供入口层主动打断） */
    public void interrupt(Integer sessionId) {
        ChatSessionVO session = chatSessionService.getSessionById(sessionId);
        HarnessAgent agent = agentRuntimeRegistry.getOrCreate(session.getBotId(), session.getSource());
        agent.getDelegate().interrupt(
                SessionKeys.userId(session),
                SessionKeys.sessionId(session, sessionId));
    }

    private void persistUserMessage(Integer sessionId, String content) {
        try {
            ChatMessageWebRequest request = new ChatMessageWebRequest();
            request.setSessionId(sessionId);
            request.setContent(content);
            request.setRole(ROLE_USER);
            chatMessageWebService.createMessage(request);
        } catch (Exception e) {
            log.error("Failed to persist user message for session: {}", sessionId, e);
        }
    }

    private void persistAssistantMessage(Integer sessionId, String content) {
        if (!StringUtils.hasText(content)) {
            return;
        }
        try {
            chatMessageWebService.createAssistantMessage(sessionId, content, "{}");
        } catch (Exception e) {
            log.error("Failed to persist assistant message for session: {}", sessionId, e);
        }
    }

    /**
     * 会话标识工具：委托 AgentPathKeys 生成文件系统安全的 userId/sessionId。
     * chatId 为空（Web 端）时回退到 DB 会话 ID。
     */
    static final class SessionKeys {
        private SessionKeys() {
        }

        static String userId(ChatSessionVO session) {
            return AgentPathKeys.userId(session.getBotId(), session.getSource(),
                    session.getChatId(), String.valueOf(session.getId()));
        }

        static String sessionId(ChatSessionVO session, Integer dbSessionId) {
            return AgentPathKeys.sessionId(session.getSource(),
                    session.getChatId(), String.valueOf(session.getId()));
        }
    }
}
