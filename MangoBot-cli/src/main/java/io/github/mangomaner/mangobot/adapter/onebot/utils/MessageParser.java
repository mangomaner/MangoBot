package io.github.mangomaner.mangobot.adapter.onebot.utils;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.mangomaner.mangobot.adapter.onebot.model.segment.*;
import io.github.mangomaner.mangobot.module.file.model.domain.BotFiles;
import io.github.mangomaner.mangobot.adapter.onebot.event.message.OneBotGroupMessageEvent;
import io.github.mangomaner.mangobot.module.file.service.BotFilesService;
import io.github.mangomaner.mangobot.adapter.onebot.outbound.send.OneBotApiService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;

@Slf4j
@Component
public class MessageParser {

    private final ObjectMapper objectMapper = new ObjectMapper();
    @Resource
    private OneBotApiService oneBotApiService;
    @Resource
    private BotFilesService botFilesService;

    public String parseMessage(List<OneBotMessageSegment> segments, Long botId) {
        if (segments == null || segments.isEmpty()) {
            return "";
        }

        StringBuilder result = new StringBuilder();
        for (OneBotMessageSegment segment : segments) {
            String parsed = parseSegment(segment, botId);
            if (parsed != null && !parsed.isEmpty()) {
                result.append(parsed);
            }
        }
        return result.toString();
    }

    private String parseSegment(OneBotMessageSegment segment, Long botId) {
        try {
            String type = segment.getType();
            switch (type) {
                case "text":
                    return parseTextSegment((TextSegment) segment);
                case "at":
                    return parseAtSegment((AtSegment) segment);
                case "face":
                    return parseFaceSegment((FaceSegment) segment);
                case "file":
                    return parseFileSegment((FileSegment) segment);
                case "image":
                    return parseImageSegment((ImageSegment) segment);
                case "json":
                    return parseJsonSegment((JsonSegment) segment);
                case "video":
                    return parseVideoSegment((VideoSegment) segment);
                case "record":
                    return parseRecordSegment((RecordSegment) segment);
                case "forward":
                    return parseForwardSegment((ForwardSegment) segment, botId);
                case "reply":
                    return parseReplySegment((ReplySegment) segment, botId);
                default:
                    log.warn("Unknown message segment type: {}", type);
                    return "";
            }
        } catch (Exception e) {
            log.error("Failed to parse message segment: {}", segment, e);
            return "";
        }
    }

    private String parseReplySegment(ReplySegment segment, Long botId) {
        ReplySegment.ReplyData data = segment.getData();
        return "回复：" + oneBotApiService.getMsg(botId, Integer.parseInt(data.getId()));
    }

    private String parseTextSegment(TextSegment segment) {
        return segment.getText();
    }

    private String parseAtSegment(AtSegment segment) {
        AtSegment.AtData data = segment.getData();
        if (data == null) {
            return "";
        }
        String name = data.getName();
        String qq = data.getQq();
        if (name != null && !name.isEmpty()) {
            return "@" + name + "(" + qq + ")";
        }
        return "@" + qq;
    }

    private String parseFaceSegment(FaceSegment segment) {
        FaceSegment.FaceData data = segment.getData();
        if (data == null) {
            return "";
        }
        int subType = data.getSubType();
        String id = data.getId();
        
        if (subType == 3 && "343".equals(id)) {
            return "害怕的表情";
        }
        if (subType == 3 && "319".equals(id)) {
            return "比心的表情";
        }
        return "表情[" + id + "]";
    }

    private String parseFileSegment(FileSegment segment) {
        FileSegment.FileData data = segment.getData();
        if (data == null) {
            return "";
        }
        String file = data.getFile();
        if (file != null && !file.isEmpty()) {
            return "文件：" + file;
        }
        return "文件";
    }

    private String parseImageSegment(ImageSegment segment) {
        ImageSegment.ImageData data = segment.getData();
        if (data == null) {
            return "";
        }
        int subType = data.getSubType();
        String url = data.getUrl();

        BotFiles fileByFileId = botFilesService.getFileByFileId(data.getFile());
        if (fileByFileId != null && fileByFileId.getDescription() != null) {
            return fileByFileId.getDescription();
        }

        return switch (subType) {
            case 0 -> {
                if (url != null && !url.isEmpty()) {
                    yield "发送图片：" + url;
                }
                yield "发送图片";
            }
            case 1, 11 -> {
                if (url != null && !url.isEmpty()) {
                    yield "发送表情：" + url;
                }
                yield "发送表情";
            }
            default -> "发送图片";
        };
    }

    private String parseJsonSegment(JsonSegment segment) {
        JsonSegment.JsonData data = segment.getData();
        if (data == null) {
            return "";
        }
        String jsonStr = data.getData();
        if (jsonStr == null || jsonStr.isEmpty()) {
            return "";
        }
        
        try {
            JsonNode jsonNode = objectMapper.readTree(jsonStr);
            StringBuilder result = new StringBuilder();
            if(jsonNode.has("prompt")){
                result.append(jsonNode.get("prompt").asText()).append(" ");
            } else {
                jsonNode.fields().forEachRemaining(entry -> {
                    result.append(entry.getKey()).append(":").append(entry.getValue().asText()).append(" ");
                });
            }

            return result.toString();
        } catch (Exception e) {
            log.error("Failed to parse JSON segment: {}", jsonStr, e);
            return jsonStr;
        }
    }

    private String parseVideoSegment(VideoSegment segment) {
        VideoSegment.VideoData data = segment.getData();

        if (data == null) {
            return "";
        }
        String url = data.getUrl();

        if (url != null && !url.isEmpty()) {
            return "视频：" + url;
        }
        return "视频";
    }

    private String parseRecordSegment(RecordSegment segment) {
        RecordSegment.RecordData data = segment.getData();
        if (data == null) {
            return "";
        }
        String url = data.getUrl();

        if (url != null && !url.isEmpty()) {
            return "语音：" + url;
        }
        return "语音";
    }

    private String parseForwardSegment(ForwardSegment segment, Long botId) {
        ForwardSegment.ForwardData data = segment.getData();
        if (data == null) {
            return "";
        }
        String id = data.getId();

        if (id == null || id.isEmpty()) {
            return "转发消息";
        }

        List<OneBotGroupMessageEvent> event = null;
        event = oneBotApiService.getForwardMsg(botId, id);

        if (event == null || event.isEmpty()) {
            return "转发消息：[合并转发消息 ID=" + id + "]";
        }

        StringBuilder sb = new StringBuilder();
        for (OneBotGroupMessageEvent e : event) {
            String message = parseMessage(e.getMessage(), botId);
            sb.append(e.getSender().getNickname()).append("发送消息：").append(message).append("\n");
        }
        String result = sb.toString();

        if(!result.isEmpty()){
            return "转发消息：\n" + result;
        } else {
            return "转发消息：[合并转发消息 ID=" + id + "]";
        }
    }
}
