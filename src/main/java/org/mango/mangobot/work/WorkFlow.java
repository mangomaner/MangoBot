package org.mango.mangobot.work;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import dev.langchain4j.community.model.dashscope.QwenChatModel;
import dev.langchain4j.community.model.dashscope.QwenChatRequestParameters;
import dev.langchain4j.data.message.UserMessage;
import dev.langchain4j.model.chat.request.ChatRequest;
import dev.langchain4j.model.chat.response.ChatResponse;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.mango.mangobot.knowledgeLibrary.service.EsTextProcessingService;
import org.mango.mangobot.manager.crawler.SearchByBrowser;
import org.mango.mangobot.service.EsDocumentService;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.List;
import java.util.Map;

/**
 * 工作流，
 */
@Component
@Slf4j
public class WorkFlow {
    @Resource
    private QwenChatModel qwenChatModel;
    @Resource
    private ObjectMapper objectMapper;
    @Resource
    private EsTextProcessingService esTextProcessingService;
    @Resource
    private EsDocumentService esDocumentService;
    @Resource
    private SearchByBrowser searchByBrowser;

    // 用prompt规范大模型输出，需返回json，且包含 canAns字段 和 message字段
    public String start(String question, int level, String groupId){
        // 1. 判断是否可以直接回答（涉及 人物/作品 的问题canAns=false，并提取出问题中的 人物/作品 作为message）
        String step1Prompt = String.format(
                "请严格按此格式输出：{\"canAns\": , \"message\": \"\"}；若问题涉及人物/作品，ans=false，message=[人物/作品名称]，否则canAns=true,message=[你的回答]。问题：%s",
                question
        );
        String step1Response = chatWithModel(step1Prompt);
        log.info("step1Response: {}", step1Response);
        Map<String, Object> step1Result = parseJson(step1Response);
        boolean canAns = (boolean) step1Result.get("canAns");
        String messageStep1 = (String) step1Result.get("message");

        if (canAns) {
            return messageStep1; // 直接返回答案
        }

        // 2. 搜索知识库，将 问题和资料 发给大模型。若 Level 等级较高，或大模型仍无法回答，则message返回 3个针对于该资料和原问题的 提问
        List<Map<String, Object>> knowledge = esDocumentService.fullTextSearch("knowledge_library", question, messageStep1, 5); // 假设已有搜索方法
        StringBuilder knowledgeText = new StringBuilder();
        for(Map<String, Object> k : knowledge){
            knowledgeText.append(k.get("content")).append("\n");
        }
        String step2Prompt = String.format(
                "请严格按此格式输出：{\"canAns\": , \"message\": \"\"}；若资料与问题相关性不高，canAns=false，message=[你对3个针对问题的进一步提问] ；否则canAns=true，message=[你的回答]。当前问题：%s，资料：%s",
                question, knowledgeText
        );
        String step2Response = chatWithModel(step2Prompt);
        log.info("step2Response: {}", step2Response);
        Map<String, Object> step2Result = parseJson(step2Response);
        canAns = (boolean) step2Result.get("canAns");
        String messageStep2 = step2Result.get("message").toString();
        if (canAns){
            return messageStep2;
        }

        // 3. 根据messageStep1(人物/作品)和messageStep2(三个问题)调用 爬虫方法b 爬取浏览器搜索，结果入库。将 问题和资料 发给大模型，获取回复

        // 调用爬虫方法a获取数据并入库
        String browserData = null;
        try {
            browserData = searchByBrowser.searchBing(messageStep1);
        } catch (IOException e) {
            browserData = "failed";
            log.error("爬虫执行失败", e);
        }
        esTextProcessingService.processTextContent(browserData);
        // 将问题和资料发给大模型
        String step3Prompt = String.format(
                "请严格按此格式输出：{\"ans\": \"\"}；ans=[你的回答]。问题 %s ;资料 %s",
                question, browserData.substring(0, Math.min(10000, browserData.length()))
        );
        String finalAnswer = chatWithModel(step3Prompt);
        return parseJson(finalAnswer).get("ans").toString();


        // 4. 根据设定的性格和人设，将上文的回复和问题再次发送给大模型，获取最终回复

    }

    private String chatWithModel(String question) {
        ChatRequest request = ChatRequest.builder()
                .messages(UserMessage.from(question))
                .parameters(QwenChatRequestParameters.builder()
                        .temperature(0.5)
                        .modelName("qwen-turbo") // 设置模型名称
                        .enableSearch(false)
                        .build())
                .build();
        ChatResponse chatResponse = qwenChatModel.chat(request);
        return chatResponse.aiMessage().text();
    }
    // JSON解析工具（需替换为实际解析逻辑）
    private Map<String, Object> parseJson(String json) {
        if(json.charAt(1) == '`'){
            json = json.substring(7, json.length() - 3);
        }
        try {
            return objectMapper.readValue(json, Map.class);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }
}
