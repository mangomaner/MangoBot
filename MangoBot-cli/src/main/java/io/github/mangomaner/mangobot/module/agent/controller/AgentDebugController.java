package io.github.mangomaner.mangobot.module.agent.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.mangomaner.mangobot.module.agent.middleware.AgentContextCapture;
import io.github.mangomaner.mangobot.system.common.BaseResponse;
import io.github.mangomaner.mangobot.system.common.ErrorCode;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Agent 调试接口：查看最近一次模型调用发送给模型的完整上下文（分析 token 消耗）
 */
@Slf4j
@RestController
@RequestMapping("/api/agent/debug")
@Tag(name = "Agent 调试")
@RequiredArgsConstructor
public class AgentDebugController {

    private final AgentContextCapture agentContextCapture;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @GetMapping("/context")
    @Operation(summary = "查看最近一次模型调用的完整上下文", description = "返回发送给模型的完整消息列表、工具清单与 Token 用量")
    public BaseResponse<Map<String, Object>> getContext(@RequestParam Integer sessionId) {
        AgentContextCapture.CallContext context = agentContextCapture.get(sessionId);
        if (context == null) {
            return new BaseResponse<>(ErrorCode.NOT_FOUND_ERROR.getCode(), null,
                    "暂无该会话的上下文记录（先发起一次对话）");
        }
        try {
            Map<String, Object> result = new LinkedHashMap<>();
            result.put("input", objectMapper.readValue(context.inputJson, Map.class));
            result.put("usage", context.usageJson != null
                    ? objectMapper.readValue(context.usageJson, Map.class)
                    : null);
            return new BaseResponse<>(0, result, "");
        } catch (Exception e) {
            log.error("Failed to read captured context for session: {}", sessionId, e);
            return new BaseResponse<>(ErrorCode.SYSTEM_ERROR.getCode(), null, "读取上下文失败");
        }
    }
}
