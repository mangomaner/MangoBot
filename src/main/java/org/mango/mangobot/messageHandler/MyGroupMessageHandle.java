package org.mango.mangobot.messageHandler;

import cn.hutool.core.lang.UUID;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.mango.mangobot.service.impl.GroupMessageService;
import org.mango.mangobot.utils.FileUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.io.*;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;

@Component
@Slf4j
public class MyGroupMessageHandle implements GroupMessageHandler {


    @Resource
    private GroupMessageService groupMessageService;
    @Resource
    private WorkFlow workFlow;
    @Resource
    private RestTemplate restTemplate;
    @Resource
    private ObjectMapper objectMapper;
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

        String result = null;
        if(targetId.equals(botQQ)) {
            if(content.trim().isEmpty()){
                String[] respons = new String[]{"？？？", "干什么！", "?"};
                groupMessageService.sendTextMessage(groupId, respons[random.nextInt(respons.length)]);
                return;
            }
            try {
                result = workFlow.startNew(content);
                groupMessageService.sendTextMessage(groupId, result);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        } else if(content.contains("setu")){
            String tag = content.replace("setu", "").trim();
            if(true){
                ResponseEntity<String> response = null;
                if(tag != "")
                    response = restTemplate.getForEntity("https://api.lolicon.app/setu/v2?r18=0&tag="+tag, String.class);
                else
                    response = restTemplate.getForEntity("https://api.lolicon.app/setu/v2?r18=0", String.class);
                // 解析JSON字符串为JsonNode
                JsonNode rootNode = null;
                try {
                    rootNode = objectMapper.readTree(response.getBody());
                } catch (JsonProcessingException e) {
                    throw new RuntimeException(e);
                }
                // 检查"data"数组是否至少有一个元素
                if (rootNode.has("data") && rootNode.get("data").isArray() && rootNode.get("data").size() > 0) {
                    // 获取第一个"data"元素
                    JsonNode firstDataNode = rootNode.path("data").get(0);
                    // 获取"urls"节点
                    if (firstDataNode.has("urls")) {
                        JsonNode urlsNode = firstDataNode.get("urls");
                        // 获取"original" URL
                        if (urlsNode.has("original")) {
                            String url = urlsNode.get("original").asText();
                            // 发送请求获取图片输入流
                            ResponseEntity<byte[]> imageResponse = restTemplate.exchange(url, HttpMethod.GET, new HttpEntity<>(new HttpHeaders()), byte[].class);
                            byte[] imageBytes = imageResponse.getBody();

                            String absolutPath = null;
                            if (imageBytes != null) {
                                // 创建保存图片的文件路径
                                String fileName = UUID.randomUUID().toString().replace("-", "") + ".jpg";
                                File savePath = new File(new FileUtils().getJarPath() + "/resource/img", fileName);

                                // 使用try-with-resources确保OutputStream被正确关闭
                                try (OutputStream os = new FileOutputStream(savePath)) {
                                    os.write(imageBytes);
                                } catch (IOException e){
                                    e.printStackTrace();
                                }
                                absolutPath = savePath.getAbsolutePath();

                            }
                            if(absolutPath == null)
                                groupMessageService.sendImageMessage(groupId, url);
                            else
                                groupMessageService.sendImageMessage(groupId, absolutPath);
                        }
                    }
                } else {
                    groupMessageService.sendTextMessage(groupId, "没有找到 %s 的图片喵".formatted(tag));
                }
            }
//            else {
//                ResponseEntity<String> response = restTemplate.getForEntity("https://t.alcy.cc/ycy/?json", String.class);
//                String url = response.getBody();
//                // 发送请求获取图片输入流
//                ResponseEntity<byte[]> imageResponse = restTemplate.exchange(url, HttpMethod.GET, new HttpEntity<>(new HttpHeaders()), byte[].class);
//                byte[] imageBytes = imageResponse.getBody();
//
//                String absolutPath = null;
//                if (imageBytes != null) {
//                    // 创建保存图片的文件路径
//                    String fileName = UUID.randomUUID().toString() + ".jpg";
//                    File savePath = new File(new FileUtils().getJarPath() + "/resource/img", fileName);
//
//                    // 使用try-with-resources确保OutputStream被正确关闭
//                    try (OutputStream os = new FileOutputStream(savePath)) {
//                        os.write(imageBytes);
//                    } catch (IOException e){
//                        e.printStackTrace();
//                    }
//                    absolutPath = savePath.getAbsolutePath();
//
//                }
//                if(absolutPath == null)
//                    groupMessageService.sendImageMessage(groupId, url);
//                else
//                    groupMessageService.sendImageMessage(groupId, absolutPath);
//            }

        }


    }

    @Override
    public void handlePoke(String fromUser, String targetUser, String groupId) {
        if(targetUser == null || !targetUser.equals(botQQ))
            return;
        String response = pokeResponses.get(random.nextInt(pokeResponses.size()));
        groupMessageService.sendTextMessage(groupId, response);
    }
}