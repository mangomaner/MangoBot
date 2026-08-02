package io.github.mangomaner.mangobot.module.agent.controller;

import io.github.mangomaner.mangobot.module.agent.chat.ChatOrchestrator;
import io.github.mangomaner.mangobot.module.agent.model.dto.CreateChatSessionRequest;
import io.github.mangomaner.mangobot.module.agent.model.dto.StreamChatRequest;
import io.github.mangomaner.mangobot.module.agent.model.enums.SessionSource;
import io.github.mangomaner.mangobot.module.agent.model.vo.ChatSessionVO;
import io.github.mangomaner.mangobot.module.agent.service.ChatSessionService;
import io.github.mangomaner.mangobot.system.common.BaseResponse;
import io.github.mangomaner.mangobot.system.common.ResultUtils;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;

/**
 * 对话控制器
 * <p>
 * 提供流式对话接口。Agent 由 AgentRuntimeRegistry 按 Bot 常驻管理，
 * 控制器只负责组装会话与消息。
 */
@Slf4j
@RestController
@RequestMapping("/api/chat")
@RequiredArgsConstructor
public class ChatController {

    private final ChatOrchestrator chatOrchestrator;
    private final ChatSessionService chatSessionService;

    @PostMapping("/session")
    @Operation(summary = "创建Web会话", description = "创建一个新的web端对话会话")
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
     * 3. 基于 AgentState 的会话上下文记忆
     * 4. 自动消息持久化
     *
     * @param request 流式对话请求
     * @return 流式响应（SSE 格式）
     */
    @PostMapping(value = "/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    @Operation(summary = "流式对话", description = "使用 Server-Sent Events 实现流式输出，支持上下文记忆")
    public Flux<String> streamChat(@Valid @RequestBody StreamChatRequest request) {
        Integer sessionId = request.getSessionId();
        String message = request.getMessage();

        log.info("Received streaming chat request, sessionId: {}, message length: {}",
                sessionId, message != null ? message.length() : 0);

        return chatOrchestrator.streamChat(sessionId, message);
    }
}
