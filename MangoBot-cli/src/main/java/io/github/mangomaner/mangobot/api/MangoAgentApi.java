package io.github.mangomaner.mangobot.api;

import io.agentscope.core.ReActAgent;
import io.github.mangomaner.mangobot.agent.factory.AgentFactory;
import io.github.mangomaner.mangobot.agent.model.enums.SessionSource;
import io.github.mangomaner.mangobot.agent.model.vo.ChatSessionVO;
import io.github.mangomaner.mangobot.agent.service.ChatSessionService;
import io.github.mangomaner.mangobot.agent.workspace.ChatService;
import reactor.core.publisher.Flux;

/**
 * Agent API (静态工具类)
 * 提供会话管理、Agent 创建、Agent 对话三大核心能力。
 */
public class MangoAgentApi {

    private static ChatSessionService chatSessionService;
    private static AgentFactory agentFactory;
    private static ChatService chatService;

    private MangoAgentApi() {}

    static void setChatSessionService(ChatSessionService service) {
        MangoAgentApi.chatSessionService = service;
    }

    static void setAgentFactory(AgentFactory factory) {
        MangoAgentApi.agentFactory = factory;
    }

    static void setChatService(ChatService service) {
        MangoAgentApi.chatService = service;
    }

    private static void checkServices() {
        if (chatSessionService == null || agentFactory == null || chatService == null) {
            throw new IllegalStateException("MangoAgentApi has not been initialized yet.");
        }
    }

    // ==================== 会话管理 ====================

    /**
     * 根据 botId 和 chatId 获取会话详情
     *
     * @param botId  Bot ID
     * @param chatId 群聊ID/私聊ID
     * @param source 会话来源
     * @return 会话视图对象，如果不存在则返回 null
     */
    public static ChatSessionVO getSessionByBotIdAndChatId(Long botId, Long chatId, SessionSource source) {
        checkServices();
        return chatSessionService.getSessionByBotIdAndChatId(botId, chatId, source);
    }

    /**
     * 根据ID获取会话详情
     *
     * @param sessionId 会话ID
     * @return 会话视图对象
     */
    public static ChatSessionVO getSessionById(Integer sessionId) {
        checkServices();
        return chatSessionService.getSessionById(sessionId);
    }

    // ==================== Agent 工厂 ====================

    /**
     * 创建 Agent
     *
     * @param sessionId 会话 ID
     * @return ReActAgent 实例
     */
    public static ReActAgent createAgent(Integer sessionId) {
        checkServices();
        return agentFactory.createAgent(sessionId);
    }

    /**
     * 创建带系统提示词的 Agent
     *
     * @param sessionId 会话 ID
     * @param prompt    系统提示词
     * @return ReActAgent 实例
     */
    public static ReActAgent createAgentWithPrompt(Integer sessionId, String prompt) {
        checkServices();
        return agentFactory.createAgentWithPrompt(sessionId, prompt);
    }

    // ==================== Agent 对话 ====================

    /**
     * 执行流式对话
     * <p>
     * 该方法会：
     * 1. 为指定会话创建独立的 Agent 实例
     * 2. 加载会话历史消息作为上下文
     * 3. 将用户消息持久化到数据库
     * 4. 通过 SSE 流式返回 AI 响应
     * 5. 自动将 AI 回复持久化到数据库
     *
     * @param sessionId 对话会话ID
     * @param message   用户消息内容
     * @param agent     ReActAgent 实例
     * @return 流式响应（Server-Sent Events）
     */
    public static Flux<String> streamChat(Integer sessionId, String message, ReActAgent agent) {
        checkServices();
        return chatService.streamChat(sessionId, message, agent);
    }
}
