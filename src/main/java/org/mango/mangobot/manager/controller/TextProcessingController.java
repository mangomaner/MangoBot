package org.mango.mangobot.manager.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import dev.langchain4j.community.model.dashscope.QwenChatModel;
import dev.langchain4j.community.model.dashscope.QwenChatRequestParameters;
import dev.langchain4j.data.message.UserMessage;
import dev.langchain4j.model.chat.request.ChatRequest;
import dev.langchain4j.model.chat.response.ChatResponse;
import dev.langchain4j.service.AiServices;
import jakarta.annotation.Resource;
import org.mango.mangobot.common.BaseResponse;
import org.mango.mangobot.common.ResultUtils;
import org.mango.mangobot.knowledgeLibrary.service.EsTextProcessingService;
import org.mango.mangobot.knowledgeLibrary.service.TextProcessingService;
import org.mango.mangobot.manager.crawler.SearchByBrowser;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.List;
import java.util.Map;

@RestController
public class TextProcessingController {

    @Resource
    private TextProcessingService textProcessingService;
    @Resource
    private QwenChatModel qwenChatModel;
    @Resource
    private ObjectMapper objectMapper;
    @Resource
    private EsTextProcessingService esTextProcessingService;



    @Resource
    SearchByBrowser searchByBrowser;
    @GetMapping("/searchByBrowser")
    public BaseResponse<String> searchByBrowser(@RequestParam String query) throws IOException {

        String result = searchByBrowser.searchBing(query);
        return ResultUtils.success(result);

    }

    @GetMapping("/query")
    public BaseResponse<List<String>> query(@RequestParam String query,
                                            @RequestParam(defaultValue = "10") int maxResults) {

        List<String> results = esTextProcessingService.queryVectorDatabase(query, maxResults);
        return ResultUtils.success(results);

    }

    @PostMapping("/chatTest")
    public String qwenChatModelTest(@RequestBody String chatInfo) {

        ChatRequest request = ChatRequest.builder()
                .messages(UserMessage.from(chatInfo))
                .parameters(QwenChatRequestParameters.builder()
                        .temperature(0.5)
                        .modelName("qwen-turbo") // 设置模型名称
                        .enableSearch(true)
                        .build())
                .build();
        ChatResponse chatResponse = qwenChatModel.chat(request);
        // 假设你想返回聊天响应的消息部分
        return chatResponse.aiMessage().text();
    }
    @PostMapping("/processStringData")
    public String processStringData(@RequestBody Object inputContent) {
        String input = (String) inputContent;
        String result = esTextProcessingService.processTextContent(input);
        return result;
    }
    @GetMapping("/processTextFiles")
    public String processTextFiles(@RequestParam String directoryPath) {
        esTextProcessingService.processTextFiles();
        return "Files processed successfully.";
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








    String firstQuestion = """
            输出json格式：
            if 涉及人物或名词解释，输出:
            {
            	canReply: false,
            	question: 提问关键词
            }
            else 输出:
            {
            	canReply: true,
            	answer: 用猫娘的语气回答
            }
            我的问题是：
            """;
    private String workFlow(String question) {
        // 第一次询问，获取是否需要再次询问的标志
        String answer = chatWithModel(firstQuestion + question);
        // 去除前后的代码块标记
        String cleanJson = answer
                .replace("```json", "")
                .replace("```", "")
                .trim();  // 移除首尾空格和换行

        // 使用 Jackson 的 ObjectMapper 将 JSON 字符串解析为 Map
        ObjectMapper mapper = new ObjectMapper();
        Map<String, Object> map = null;
        try {
            map = mapper.readValue(cleanJson, Map.class);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
        if(map.get("canReply").equals(true)){
            return (String)map.get("answer");
        }
        // 第二次询问，获取答案
        String preparedToSearch = (String)map.get("question");


        // 输出结果
        System.out.println(map);
        return "";
    }

    private String chatWithModel(String question) {
        ChatRequest request = ChatRequest.builder()
                .messages(UserMessage.from(question))
                .parameters(QwenChatRequestParameters.builder()
                        .temperature(0.5)
                        .modelName("qwen-turbo") // 设置模型名称
                        .enableSearch(true)
                        .build())
                .build();
        ChatResponse chatResponse = qwenChatModel.chat(request);
        return chatResponse.aiMessage().text();
    }
}