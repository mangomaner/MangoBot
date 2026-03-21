package io.github.mangomaner.mangobot.web_controller.model_test;

import io.agentscope.core.message.ContentBlock;
import io.agentscope.core.message.Msg;
import io.agentscope.core.message.MsgRole;
import io.agentscope.core.message.TextBlock;
import io.agentscope.core.model.ChatResponse;
import io.agentscope.core.model.OpenAIChatModel;
import io.github.mangomaner.mangobot.system.common.BaseResponse;
import io.github.mangomaner.mangobot.system.common.ResultUtils;
import io.github.mangomaner.mangobot.module.configuration.core.ModelProvider;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/test/model")
@Slf4j
public class ModelTestController {

    private final ModelProvider modelProvider;

    public ModelTestController(ModelProvider modelProvider) {
        this.modelProvider = modelProvider;
    }

    @GetMapping("/chat")
    public BaseResponse<Map<String, Object>> chat(
            @RequestParam(defaultValue = "main") String role,
            @RequestParam String message) {
        
        Map<String, Object> result = new HashMap<>();
        
        OpenAIChatModel model = modelProvider.getModel(role);
        if (model == null) {
            result.put("error", "Model not found for role: " + role);
            return ResultUtils.success(result);
        }
        
        result.put("role", role);
        result.put("inputMessage", message);
        result.put("modelHashCode", System.identityHashCode(model));
        
        try {
            List<Msg> messages = new ArrayList<>();
            messages.add(Msg.builder()
                    .role(MsgRole.USER)
                    .content(TextBlock.builder().text(message).build())
                    .build());
            
            StringBuilder fullContent = new StringBuilder();
            model.stream(messages, null, null)
                .doOnNext(resp -> {
                    String text = extractText(resp);
                    if (text != null && !text.isEmpty()) {
                        fullContent.append(text);
                    }
                    System.out.println(message + "  Response:  " + text);
                })
                .blockLast();
            
            result.put("response", fullContent.toString());
            
        } catch (Exception e) {
            result.put("error", e.getMessage());
            log.error("Chat failed", e);
        }
        
        return ResultUtils.success(result);
    }

    @PostMapping("/chat-multi")
    public BaseResponse<Map<String, Object>> chatMultiTurn(
            @RequestParam(defaultValue = "main") String role,
            @RequestBody List<Map<String, String>> messages) {
        
        Map<String, Object> result = new HashMap<>();
        
        OpenAIChatModel model = modelProvider.getModel(role);
        if (model == null) {
            result.put("error", "Model not found for role: " + role);
            return ResultUtils.success(result);
        }
        
        result.put("role", role);
        result.put("modelHashCode", System.identityHashCode(model));
        
        try {
            List<Msg> msgList = new ArrayList<>();
            for (Map<String, String> msg : messages) {
                MsgRole msgRole = "assistant".equalsIgnoreCase(msg.get("role")) 
                    ? MsgRole.ASSISTANT 
                    : MsgRole.USER;
                msgList.add(Msg.builder()
                        .role(msgRole)
                        .content(TextBlock.builder().text(msg.get("content")).build())
                        .build());
            }
            
            StringBuilder fullContent = new StringBuilder();
            model.stream(msgList, null, null)
                .doOnNext(resp -> {
                    String text = extractText(resp);
                    if (text != null && !text.isEmpty()) {
                        fullContent.append(text);
                    }
                })
                .blockLast();
            
            result.put("response", fullContent.toString());
            
        } catch (Exception e) {
            result.put("error", e.getMessage());
            log.error("Multi-turn chat failed", e);
        }
        
        return ResultUtils.success(result);
    }

    @GetMapping(value = "/chat-stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<String> chatStream(
            @RequestParam(defaultValue = "main") String role,
            @RequestParam String message) {
        
        OpenAIChatModel model = modelProvider.getModel(role);
        if (model == null) {
            return Flux.just("data: {\"error\": \"Model not found\"}\n\n");
        }
        
        List<Msg> messages = new ArrayList<>();
        messages.add(Msg.builder()
                .role(MsgRole.USER)
                .content(TextBlock.builder().text(message).build())
                .build());
        
        return model.stream(messages, null, null)
                .map(resp -> {
                    String text = extractText(resp);
                    if (text == null) text = "";
                    return "data: " + text.replace("\n", "\\n") + "\n\n";
                });
    }

    @GetMapping("/compare")
    public BaseResponse<Map<String, Object>> compareModels(
            @RequestParam(defaultValue = "main") String role,
            @RequestParam(defaultValue = "5") int count) {
        
        Map<String, Object> result = new HashMap<>();
        Map<Integer, Integer> hashCodes = new HashMap<>();
        
        for (int i = 0; i < count; i++) {
            OpenAIChatModel model = modelProvider.getModel(role);
            if (model != null) {
                int hashCode = System.identityHashCode(model);
                hashCodes.put(i, hashCode);
            }
        }
        
        result.put("role", role);
        result.put("requestCount", count);
        result.put("hashCodes", hashCodes);
        result.put("uniqueInstances", hashCodes.values().stream().distinct().count());
        result.put("isSingleton", hashCodes.values().stream().distinct().count() == 1);
        
        return ResultUtils.success(result);
    }

    @GetMapping("/all-roles")
    public BaseResponse<Map<String, Object>> getAllRolesInfo() {
        Map<String, Object> result = new HashMap<>();
        
        String[] roles = {"main", "assistant", "image", "embedding"};
        
        for (String role : roles) {
            OpenAIChatModel model = modelProvider.getModel(role);
            Map<String, Object> roleInfo = new HashMap<>();
            
            if (model != null) {
                roleInfo.put("exists", true);
                roleInfo.put("hashCode", System.identityHashCode(model));
            } else {
                roleInfo.put("exists", false);
            }
            
            result.put(role, roleInfo);
        }
        
        return ResultUtils.success(result);
    }

    private String extractText(ChatResponse response) {
        if (response == null || response.getContent() == null) {
            return null;
        }
        
        StringBuilder sb = new StringBuilder();
        for (ContentBlock block : response.getContent()) {
            if (block instanceof TextBlock) {
                TextBlock textBlock = (TextBlock) block;
                if (textBlock.getText() != null) {
                    sb.append(textBlock.getText());
                }
            }
        }
        return sb.toString();
    }
}
