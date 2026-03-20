package io.github.mangomaner.mangobot.module.agent.controller;

import io.github.mangomaner.mangobot.module.agent.factory.AgentFactory;
import io.github.mangomaner.mangobot.module.agent.model.dto.CreateChatSessionRequest;
import io.github.mangomaner.mangobot.module.agent.model.dto.StreamChatRequest;
import io.github.mangomaner.mangobot.module.agent.model.enums.SessionSource;
import io.github.mangomaner.mangobot.module.agent.model.vo.ChatSessionVO;
import io.github.mangomaner.mangobot.module.agent.workspace.ChatService;
import io.github.mangomaner.mangobot.module.agent.service.ChatSessionService;
import io.github.mangomaner.mangobot.system.common.BaseResponse;
import io.github.mangomaner.mangobot.system.common.ResultUtils;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;

/**
 * 对话控制器
 * <p>
 * 提供流式对话接口，支持基于会话ID的上下文记忆和消息持久化。
 * 每个会话拥有独立的 Agent 实例，确保会话隔离。
 */
@Slf4j
@RestController
@RequestMapping("/api/chat")
@RequiredArgsConstructor
public class ChatController {

    private final ChatService chatService;
    private final ChatSessionService chatSessionService;
    private final AgentFactory agentFactory;

    @PostMapping("/session")
    @Operation(summary = "创建Web会话", description = "创建一个新的Web端对话会话")
    public BaseResponse<ChatSessionVO> createWebSession(@RequestBody CreateChatSessionRequest request) {
        request.setSource(SessionSource.WEB);
        log.info("创建Web会话，title: {}", request.getTitle());
        ChatSessionVO session = chatSessionService.createSession(request);
        return ResultUtils.success(session);
    }

    /**
     * 流式对话
     * <p>
     * 使用 Server-Sent Events 实现流式输出，支持：
     * 1. 实时显示 AI 推理过程
     * 2. 工具调用信息展示
     * 3. 基于会话历史的上下文记忆
     * 4. 自动消息持久化
     *
     * @param request 流式对话请求，包含会话ID和消息内容
     * @return 流式响应（SSE 格式）
     */
    @PostMapping(value = "/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    @Operation(summary = "流式对话", description = "使用 Server-Sent Events 实现流式输出，支持上下文记忆")
    public Flux<String> streamChat(@Valid @RequestBody StreamChatRequest request) {
        Integer sessionId = request.getSessionId();
        String message = request.getMessage();

        log.info("Received streaming chat request, sessionId: {}, message length: {}",
                sessionId, message != null ? message.length() : 0);



        return chatService.streamChat(sessionId, message, agentFactory.createAgent(sessionId));
    }
}