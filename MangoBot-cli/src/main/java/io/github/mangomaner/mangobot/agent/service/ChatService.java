package io.github.mangomaner.mangobot.agent.service;

import reactor.core.publisher.Flux;

/**
 * 对话服务接口
 * <p>
 * 提供流式对话功能，支持基于会话ID的上下文记忆和消息持久化。
 */
public interface ChatService {

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
     * @return 流式响应（Server-Sent Events）
     */
    Flux<String> streamChat(Integer sessionId, String message);
}
