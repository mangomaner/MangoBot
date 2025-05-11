package org.mango.mangobot.manager.controller;

import dev.langchain4j.agent.tool.Tool;
import dev.langchain4j.community.model.dashscope.QwenChatModel;
import dev.langchain4j.community.model.dashscope.QwenChatRequestParameters;
import dev.langchain4j.data.message.UserMessage;
import dev.langchain4j.model.chat.request.ChatRequest;
import dev.langchain4j.model.chat.response.ChatResponse;
import dev.langchain4j.service.AiServices;
import jakarta.annotation.Resource;
import org.mango.mangobot.common.BaseResponse;
import org.mango.mangobot.common.ErrorCode;
import org.mango.mangobot.common.ResultUtils;
import org.mango.mangobot.exception.BusinessException;
import org.mango.mangobot.knowledgeLibrary.service.TextProcessingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
public class TextProcessingController {

    @Resource
    private TextProcessingService textProcessingService;
    @Resource
    private QwenChatModel qwenChatModel;


    @Tool
    public int add(int a, int b) {
        return a + b;
    }

    @GetMapping("/query")
    public BaseResponse<List<String>> query(@RequestParam String query,
                                            @RequestParam(defaultValue = "10") int maxResults) {
        try {
            List<String> results = textProcessingService.queryVectorDatabase(query, maxResults);
            return ResultUtils.success(results);
        } catch (Exception e) {
            // 处理异常情况
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "查询失败");
        }
    }

    @GetMapping("/chatTest")
    public String qwenChatModelTest(@RequestParam(required = false) String chatInfo) {
        ChatRequest request = ChatRequest.builder()
                .messages(UserMessage.from(chatInfo))
                .parameters(QwenChatRequestParameters.builder()
                        .temperature(0.5)
                        .modelName("qwen-turbo") // 设置模型名称
                        .build())
                .build();

        ChatResponse chatResponse = qwenChatModel.chat(request);
        // 假设你想返回聊天响应的消息部分
        return chatResponse.aiMessage().text();
    }
    @GetMapping("/processTextFiles")
    public String processTextFiles(@RequestParam String directoryPath) {
        try {
            textProcessingService.processTextFiles();
            return "Files processed successfully.";
        } catch (Exception e) {
            return "Error processing files: " + e.getMessage();
        }
    }

    @GetMapping("/askWithTools")
    public String askWithTools(@RequestParam String question) {
        MathGenius mathGenius = AiServices.builder(MathGenius.class)
                .chatLanguageModel(qwenChatModel)
                .tools(new Calculator())
                .build();

        //String answer = mathGenius.ask("475695037565 的平方根是多少？");
        String answer = mathGenius.ask(question);

        return answer;
    }

}