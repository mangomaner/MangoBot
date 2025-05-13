package org.mango.mangobot.messageStore;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Updates;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.bson.Document;
import org.mango.mangobot.messageStore.collection.QQMessageCollection;
import org.mango.mangobot.model.QQ.QQMessage;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Component;

import java.util.Map;

import static com.mongodb.client.model.Filters.eq;

@Component
@Slf4j
public class DatabaseHandler {

    @Resource
    private MongoClient mongoClient;

    @Resource
    private ObjectMapper objectMapper;

    @Resource
    private Map<String, QQMessage> echoMap;

    /**
     * 将消息存储到 MongoDB 中
     *
     * @param qqMessage 消息对象
     * @param groupId   群组 ID
     */
    public void saveMessageToDatabase(QQMessage qqMessage, String groupId){
        MongoDatabase database = mongoClient.getDatabase("qq_message");
        MongoCollection<Document> collection = database.getCollection("messages_group_" + groupId);

        QQMessageCollection qqMessageCollection = new QQMessageCollection();
        BeanUtils.copyProperties(qqMessage, qqMessageCollection);
        // 使用 ObjectMapper 将对象转换为 Document
        Document doc = null;
        try {
            doc = Document.parse(objectMapper.writeValueAsString(qqMessageCollection));
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }

        // 设置 MongoDB 的 _id 字段为 message_id
        if (qqMessage.getMessage_id() != null) {
            doc.put("_id", qqMessage.getMessage_id());
        } else {
            log.warn("message_id 为空，跳过该条消息");
            return;
        }

        collection.insertOne(doc);
        log.debug("消息已成功保存到集合 {}: {}", "messages_group_" + groupId, qqMessage.getRaw_message());
    }

    /**
     * 更新 MongoDB 中某条消息的撤回状态（消息撤回时调用）
     *
     * @param groupId   群ID
     * @param messageId 消息ID
     */
    public void updateRecallStatus(Long groupId, Long messageId) {
        MongoDatabase database = mongoClient.getDatabase("qq_message");
        MongoCollection<Document> collection = database.getCollection("messages_group_" + groupId);
        collection.updateOne(eq("_id", messageId), Updates.set("isDelete", true));
    }

    public void updateSentMessageEchoId(String echo, Long messageId) {
        MongoDatabase database = mongoClient.getDatabase("qq_message");
        QQMessage qqMessage = echoMap.get(echo);
        qqMessage.setMessage_id(messageId);
        if(qqMessage == null){
            log.warn("echo 为空，无法更新发送的消息");
            return;
        }
        saveMessageToDatabase(qqMessage, qqMessage.getGroup_id());
    }
}
