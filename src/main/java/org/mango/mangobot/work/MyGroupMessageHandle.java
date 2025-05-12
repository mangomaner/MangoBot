package org.mango.mangobot.work;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import dev.langchain4j.community.model.dashscope.QwenChatModel;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.Resource;
import org.mango.mangobot.messageHandler.GroupMessageHandler;
import org.mango.mangobot.service.impl.GroupMessageService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

@Component
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
        System.out.println("handleCombinationMessage: " + content + " " + fromUser + " " + groupId + " " + replyContent + " " + targetId + " " + imageUrl);
    }

    @Override
    public void handlePoke(String fromUser, String targetUser, String groupId) {
        if(targetUser == null || !targetUser.equals(botQQ))
            return;
        String response = pokeResponses.get(random.nextInt(pokeResponses.size()));
        groupMessageService.sendTextMessage(groupId, response);
    }
}