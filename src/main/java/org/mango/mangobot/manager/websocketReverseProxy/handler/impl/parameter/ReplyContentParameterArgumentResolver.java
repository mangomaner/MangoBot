package org.mango.mangobot.manager.websocketReverseProxy.handler.impl.parameter;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import jakarta.annotation.Resource;
import org.bson.Document;
import org.mango.mangobot.annotation.QQ.parameter.ReplyContent;
import org.mango.mangobot.manager.websocketReverseProxy.handler.ParameterArgumentResolver;
import org.mango.mangobot.model.QQ.QQMessage;
import org.mango.mangobot.model.QQ.ReceiveMessageSegment;
import org.mango.mangobot.utils.MethodParameter;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class ReplyContentParameterArgumentResolver implements ParameterArgumentResolver {

    @Resource
    private MongoClient mongoClient;

    @Override
    public boolean supportsParameter(MethodParameter parameter) {
        return parameter.hasAnnotation(ReplyContent.class);
    }

    @Override
    public Object resolveArgument(MethodParameter parameter, QQMessage message) {
        List<ReceiveMessageSegment> segments = message.getMessage();
        Integer messageId = null;
        for (ReceiveMessageSegment segment : segments) {
            if ("reply".equalsIgnoreCase(segment.getType())) {
                messageId = Integer.parseInt(segment.getData().getId());
                break;
            }
        }

        // 如果找到了有效的 messageId，则从数据库中获取消息内容
        if (messageId != null) {
            Document document = findMessageById(messageId, message.getGroup_id());
            if (document != null) {
                // 解析并返回需要的内容
                return parseMessageContent(document);
            }
        }

        return ""; // 或者抛出异常，取决于你的需求
    }

    private Document findMessageById(Integer messageId, String groupId) {
        MongoDatabase database = mongoClient.getDatabase("qq_message"); // 替换为你的数据库名
        MongoCollection<Document> collection = database.getCollection("messages_group_" + groupId); // 替换为你的集合名

        Document query = new Document("_id", messageId.longValue()); // MongoDB的_id字段是Long类型
        return collection.find(query).first();
    }

    private String parseMessageContent(Document document) {
        List<Document> messageList = document.getList("message", Document.class);

        StringBuilder contentBuilder = new StringBuilder();
        for (Document msgPart : messageList) {
            String type = msgPart.getString("type");
            Document data = msgPart.get("data", Document.class);

            if ("text".equals(type)) {
                contentBuilder.append(data.getString("text"));
            } else if ("at".equals(type)) {
                contentBuilder.append("@").append(data.getString("name"));
            } else if ("image".equals(type)) {
                contentBuilder.append(data.getString("url"));
            } else if ("file".equals(type)) {
                contentBuilder.append(data.getString("url"));
            }
            contentBuilder.append(" ");
        }
        return contentBuilder.toString();
    }
}
