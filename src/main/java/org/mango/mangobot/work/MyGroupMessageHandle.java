package org.mango.mangobot.work;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import dev.langchain4j.community.model.dashscope.QwenChatModel;
import dev.langchain4j.community.model.dashscope.QwenChatRequestParameters;
import dev.langchain4j.data.message.UserMessage;
import dev.langchain4j.model.chat.request.ChatRequest;
import dev.langchain4j.model.chat.response.ChatResponse;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.mango.mangobot.messageHandler.GroupMessageHandler;
import org.mango.mangobot.service.impl.GroupMessageService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

@Component
@Slf4j
public class MyGroupMessageHandle implements GroupMessageHandler {

    @Resource
    private QwenChatModel qwenChatModel;
    @Resource
    private GroupMessageService groupMessageService;
    @Value("${QQ.botQQ}")
    private String botQQ;
    private List<String> pokeResponses = new ArrayList<>();
    private final Random random = new Random();

    @PostConstruct
    public void init() {
        try (InputStream inputStream = getClass().getResourceAsStream("/work/pokeResponse.txt")) {
            if (inputStream == null) {
                System.err.println("资源文件未找到！");
                return;
            }
            ObjectMapper mapper = new ObjectMapper();
            pokeResponses = mapper.readValue(inputStream, new TypeReference<List<String>>() {});
            System.out.println("成功加载 " + pokeResponses.size() + " 条 poke 回复");
        } catch (IOException e) {
            System.err.println("无法加载或解析 pokeResponse.txt 文件！");
            e.printStackTrace();
        }
    }

    @Override
    public void handleCombinationMessage(String fromUser, String content, String groupId, String imageUrl, String replyContent, String targetId) {
        StringBuilder stringBuilder = new StringBuilder();
        if(!fromUser.isEmpty()) stringBuilder.append("消息来自:" + fromUser);
        if(!targetId.isEmpty()) stringBuilder.append("; @用户: " + targetId);
        if(!content.isEmpty()) stringBuilder.append("; 内容:" + content);
        if(!imageUrl.isEmpty()) stringBuilder.append("; 图片:" + imageUrl);
        if(!replyContent.isEmpty()) stringBuilder.append("; 回复前面的内容:" + replyContent);
        String receivedMessage = stringBuilder.toString();

        if(!targetId.equals(botQQ)) return;

        // 假设你想返回聊天响应的消息部分
        String result = chatWithModel(content);
        groupMessageService.sendTextMessage(groupId, result);
    }

    @Override
    public void handlePoke(String fromUser, String targetUser, String groupId) {
        if(targetUser == null || !targetUser.equals(botQQ))
            return;
        String response = pokeResponses.get(random.nextInt(pokeResponses.size()));
        groupMessageService.sendTextMessage(groupId, response);
    }

    private String workFlow(String question) {

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