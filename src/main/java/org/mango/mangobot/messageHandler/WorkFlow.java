package org.mango.mangobot.messageHandler;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.hankcs.hanlp.dictionary.CustomDictionary;
import com.hankcs.hanlp.suggest.Suggester;
import com.microsoft.playwright.Page;
import dev.langchain4j.community.model.dashscope.QwenChatModel;
import dev.langchain4j.community.model.dashscope.QwenChatRequestParameters;
import dev.langchain4j.data.message.UserMessage;
import dev.langchain4j.model.chat.request.ChatRequest;
import dev.langchain4j.model.chat.response.ChatResponse;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.mango.mangobot.knowledgeLibrary.service.EsTextProcessingService;
import org.mango.mangobot.manager.crawler.PlaywrightBrowser;
import org.mango.mangobot.manager.crawler.SearchByBrowser;
import org.mango.mangobot.model.document.TextDocument;
import org.mango.mangobot.service.EsDocumentService;
import org.mango.mangobot.utils.VectorUtil;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

import com.hankcs.hanlp.*;
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
    @Resource
    private PlaywrightBrowser playwrightBrowser;
    @Resource
    private VectorUtil vectorUtil;

    public synchronized String startNew(String question) throws IOException {
        // 1. 询问AI，从问题中提取关键词
        String step1Prompt = String.format(
                "请严格按json格式输出：{jud:`问题为名词或涉及人物/作品/事件=true，否则=false`,keyWords:[`问题的关键词或作品、人物名`]}；问题：%s",
                question
        );
        String step1Response = chatWithModel(step1Prompt);
        log.info("step1Response: {}", step1Response);
        Map<String, Object> step1Result = parseJson(step1Response);
        boolean jud = (boolean) step1Result.get("jud");
        List<String> messageStep1 = (List<String>) step1Result.get("keyWords");
        if(!jud){
            String cosplayPrompt = String.format("请严格按json格式输出：{ans:`请你以猫娘的语气回答问题`}；问题：%s；人设：你叫mangoman，是一只猫娘，别人在和你聊天", question);
            String cosplayResponse = chatWithModel(cosplayPrompt);
            Map<String, Object> resultMap = parseJson(cosplayResponse);
            return (String) resultMap.get("ans");
        }

        // 2. 爬虫百度搜索，获取所有搜索结果
        String searchResult = playwrightBrowser.searchBaidu(messageStep1.stream().map(s -> s + " ").collect(Collectors.joining()));

        // 3. 分词器添加第一步的关键词，并对爬虫结果进行分词，取最高的几个。至此获取准确的关键词
        //System.out.println(HanLP.segment("你好，欢迎使用HanLP汉语处理包！"));
        for(String s : messageStep1){
            CustomDictionary.insert(s);
        }
        List<String> keywordList = HanLP.extractKeyword(searchResult, 5);
        keywordList = removeStopWords(keywordList);

        log.info("keyWordsList:{}",keywordList);
        String keyWords = keywordList.stream().map(s -> s + " ").collect(Collectors.joining());

        // 4. 根据关键词搜索知识库，若相关度较低则按关键词搜索百科，并加入知识库(存储文章全部内容)
        List<Map<String, Object>> knowledgeList = esDocumentService.fullTextSearch("knowledge_library", keyWords, "", 4);
        StringBuilder knowledgeText = new StringBuilder();
        double highestScore = 0;
        // 对评分进行归一化处理
        if(!knowledgeList.isEmpty())
            highestScore = (double) knowledgeList.get(0).get("score");
        for(Map<String, Object> k : knowledgeList){
            // 若相似度 > 85% 则认为是与当前内容相关
            if((double) k.get("score") /  highestScore > 0.85)
                knowledgeText.append(k.get("content")).append("\n");
        }
        List<String> checkList = HanLP.extractKeyword(knowledgeText.toString(), 5);
        checkList = removeStopWords(checkList);
        log.info("checkList:{}", checkList);

        // 判断关键词相关性 (防止刚才从知识库搜索出的内容全都是低分不相关内容)
        String article = knowledgeText.toString();
        double jaccardSimilarity = jaccardSimilarity(checkList, keywordList);
        if(jaccardSimilarity < 0.3){
            // 若相关度低，从搜索结果获取作品名
            String step5Prompt = String.format(
                    "请严格按json格式输出：{name:`文本提到最多的作品名或人名`}；文本：%s",
                    searchResult.substring(0, 100)
            );
            String step5Response = chatWithModel(step5Prompt);
            Map<String, Object> step5Result = parseJson(step5Response);
            String name = (String) step5Result.get("name");
            log.info("作品名: {}", name);
            // 搜索百度百科后添加到知识库
            article = playwrightBrowser.searchBaiduBaike(name);
            if(article == null)
                article = searchResult;
            esTextProcessingService.processTextContent(article, name, 5000);
        }

        // 5. 对文章进行段落分割，取相关性最高的几段
        List<String> paragraphs = VectorUtil.splitByParagraph(article, 500, "");
        // 对分割出的段落进行打分
        Suggester suggester = new Suggester();
        for (String paragraph : paragraphs) {
            suggester.addSentence(paragraph);
        }
        List<String> mostRelevantParagraphs = suggester.suggest(question, 3);

        // 6. 将问题和知识库发给AI，整理结果。
        String step6Prompt = String.format(
                "请严格按json格式输出：{ans:`用猫娘的语气回答问题`}；问题：%s；你知道的：%s",
                question, mostRelevantParagraphs.stream().map(s -> s + " ").collect(Collectors.joining())
        );
        String aiResponse = chatWithModel(step6Prompt);
        log.info("Final AI Response: {}", aiResponse);
        Map<String, Object> step6Response = parseJson(aiResponse);
        String ans = (String) step6Response.get("ans");


//        // 7. 按角色设定回答
//        String cosplayPrompt = String.format("请严格按json格式输出：{ans:`请你以猫娘的方式叙述文本`}；文本：%s；人设：你叫mangoman，是一只猫娘，别人在向你提问", ans);
//        String cosplayResponse = chatWithModel(cosplayPrompt);
//        Map<String, Object> resultMap = parseJson(cosplayResponse);
        return ans;
    }


    // 用prompt规范大模型输出，需返回json，且包含 canAns字段 和 message字段
    public String start(String question, int level){
        // 1. 判断是否可以直接回答（涉及 人物/作品 的问题canAns=false，并提取出问题中的 人物/作品 作为message）
        List<Map<String, Object>> knowledge = esDocumentService.fullTextSearch("knowledge_library", question, "", 2);
        StringBuilder knowledgeText = new StringBuilder();
        for(Map<String, Object> k : knowledge){
            knowledgeText.append(k.get("content")).append("\n");
        }
        String step1Prompt = String.format(
                "请严格按此格式输出：{\"canAns\": , \"message\": \"\"}；若涉及人物/作品/事件资料未提到，ans=false，message=[人物/作品/事件名称]，否则canAns=true,message=[你的回答]。问题：%s，资料：%s",
                question, knowledgeText
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
        knowledge = esDocumentService.fullTextSearch("knowledge_library", question, messageStep1, 2);
        knowledgeText = new StringBuilder();
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
            browserData = searchByBrowser.searchMengNiang(messageStep1);
        } catch (IOException e) {
            browserData = "failed";
            log.error("爬虫执行失败", e);
        }

        // 将问题和资料发给大模型
        String step3Prompt = String.format(
                "请严格按此格式输出：{\"ans\": \"\", \"keyWords\": \"\"}；ans=[你的回答，说些细节]，keyWords=[资料的关键词]。问题 %s ;资料 %s",
                question, browserData.substring(0, Math.min(10000, browserData.length()))
        );
        String finalAnswer = chatWithModel(step3Prompt);
        String keyWords = parseJson(finalAnswer).get("keyWords").toString();
        esTextProcessingService.processTextContent(browserData, keyWords, 10000);
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
            if (json.charAt(json.length()-1) == '`'){
                json = json.substring(7, json.length()-3);
            } else {
                json = json.substring(7, json.length());
            }
        }
        try {
            return objectMapper.readValue(json, Map.class);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }
    private List<String> removeStopWords(List<String> words){
        List<String> stopWords = Arrays.asList("百度", "百科", "角色", "简介", "免费", "作品",
                "com", "分享", "图片", "小说", "-", "_", "日", "话", "插入", "漫画", "会", "木");
        return words.stream()
                .filter(word -> !stopWords.contains(word))
                .collect(Collectors.toList());
    }

    public static double jaccardSimilarity(List<String> a, List<String> b) {
        Set<String> setA = new HashSet<>(a);
        Set<String> setB = new HashSet<>(b);

        // 构建交集（模糊匹配）
        Set<String> intersection = new HashSet<>();
        for (String x : setA) {
            for (String y : setB) {
                if (isSimilar(x, y)) {
                    intersection.add(x); // 只需记录一个即可，避免重复计数
                    break;
                }
            }
        }

        // 并集 = 所有元素，无需去重，因为使用的是 Set
        Set<String> union = new HashSet<>(setA);
        union.addAll(setB);

        if (union.isEmpty()) return 1.0;

        return (double) intersection.size() / union.size();
    }

    // 判断两个字符串是否“相似”：忽略大小写、包含关系
    private static boolean isSimilar(String s1, String s2) {
        s1 = s1.toLowerCase();
        s2 = s2.toLowerCase();

        return s1.equals(s2) || s1.contains(s2) || s2.contains(s1);
    }
}
