package org.mango.mangobot.config;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mongodb.client.MongoClient;
import com.mongodb.client.model.CreateCollectionOptions;
import dev.langchain4j.community.model.dashscope.QwenChatModel;
import dev.langchain4j.community.model.dashscope.QwenEmbeddingModel;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.service.V;
import dev.langchain4j.store.embedding.EmbeddingStore;
import dev.langchain4j.store.embedding.mongodb.IndexMapping;
import dev.langchain4j.store.embedding.mongodb.MongoDbEmbeddingStore;
import org.bson.conversions.Bson;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

import java.util.HashSet;
import java.util.List;

@Configuration
public class AppConfig {

    @Value("${chat-model.api-key}")
    private String apiKey;

    @Value("${chat-model.model-name}")
    private String modelName;

    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }

    @Bean QwenChatModel qwenChatModel(){
        return QwenChatModel.builder()
                .apiKey(apiKey)
                .modelName("qwen-turbo")
                .listeners(List.of(new MyChatModelListener()))
                .build();
    }

    @Bean ObjectMapper objectMapper() {
        return new ObjectMapper().setSerializationInclusion(JsonInclude.Include.NON_NULL);
    }

//    @Bean
//    public QwenEmbeddingModel getModel() {
//        return QwenEmbeddingModel.builder()
//                .apiKey("sk-e1fcd49b360d4f088cbe1ce6b8e2f5db")
//                .modelName("text-embedding-v2")
//                .build();
//    }


}
