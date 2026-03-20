package io.github.mangomaner.mangobot.handler.response.tools;

import io.agentscope.core.tool.Tool;
import io.agentscope.core.tool.ToolParam;
import io.github.mangomaner.mangobot.annotation.MangoTool;
import io.github.mangomaner.mangobot.api.MangoFileApi;
import io.github.mangomaner.mangobot.api.MangoOneBotApi;
import io.github.mangomaner.mangobot.api.context.ChatContext;
import io.github.mangomaner.mangobot.model.domain.BotFiles;
import io.github.mangomaner.mangobot.model.onebot.MessageBuilder;
import io.github.mangomaner.mangobot.model.onebot.SendMessage;
import io.github.mangomaner.mangobot.service.BotFilesService;
import io.github.mangomaner.mangobot.utils.FileUtils;
import org.springframework.stereotype.Component;

import jakarta.annotation.Resource;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

@Component
@MangoTool(name = "GroupImageSend", description = "发送群聊表情", category = "Group")
public class GroupImageSendTool {

    private final Random random = new Random();

    @Tool(description = "获取可发送的表情包列表，返回表情包的fileId和描述信息")
    public String getMemeImages() {
        List<BotFiles> memeFiles = MangoFileApi.getFilesByType("meme");
        
        if (memeFiles == null || memeFiles.isEmpty()) {
            return "当前没有可用的表情包";
        }
        
        java.util.Collections.shuffle(memeFiles, random);
        int count = Math.min(3, memeFiles.size());
        
        StringBuilder result = new StringBuilder("推荐的表情包：\n");
        for (int i = 0; i < count; i++) {
            BotFiles file = memeFiles.get(i);
            result.append(String.format("%d. fileName: %s, 描述: %s\n",
                    i + 1, 
                    file.getFileId(), 
                    file.getDescription() != null ? file.getDescription() : "无描述"));
        }
        result.append("\n使用 sendMemeImage 工具发送表情包，传入对应的 fileName");
        
        return result.toString();
    }

    @Tool(description = "发送表情包图片到群聊")
    public String sendMemeImage(
            @ToolParam(name = "fileName", description = "要发送的表情包fileName，从getMemeImages获取（格式为xxxxx.xxx）")
            String fileId,
            ChatContext context
    ) {
        BotFiles file = MangoFileApi.getFileByFileId(fileId);
        
        if (file == null || !"meme".equals(file.getFileType())) {
            return "表情包不存在或fileName无效，请先调用getMemeImages获取可用表情包（格式为xxxxx.xxx）";
        }
        
        String imagePath = file.getFilePath();
        if (imagePath == null || imagePath.isEmpty()) {
            return "表情包文件路径无效";
        }
        
        SendMessage sendMessage = MessageBuilder.create()
                .image(FileUtils.getBaseDirectory().normalize().toString().replace('\\', '/') + "/" + imagePath, true)
                .build();
        
        MangoOneBotApi.sendGroupMsg(context.getBotId(), context.getChatId(), sendMessage);
        return "表情包发送成功";
    }
}
