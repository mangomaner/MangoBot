package org.mango.mangobot.knowledgeLibrary.service;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.io.file.PathUtil;
import cn.hutool.core.util.StrUtil;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import dev.langchain4j.community.model.dashscope.QwenEmbeddingModel;
import dev.langchain4j.data.document.Document;
import dev.langchain4j.data.document.Metadata;
import dev.langchain4j.data.document.splitter.DocumentByParagraphSplitter;
import dev.langchain4j.data.embedding.Embedding;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.store.embedding.EmbeddingMatch;
import dev.langchain4j.store.embedding.EmbeddingSearchRequest;
import dev.langchain4j.store.embedding.EmbeddingSearchResult;

import dev.langchain4j.store.embedding.mongodb.IndexMapping;
import dev.langchain4j.store.embedding.mongodb.MongoDbEmbeddingStore;
import jakarta.annotation.Resource;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.util.ResourceUtils;
import org.springframework.web.client.RestTemplate;


import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

@Service
@Slf4j
public class TextProcessingService {

    @Resource
    private RestTemplate restTemplate;

    @Value("chat-model.api-key")
    private String apiKey;

    private ObjectMapper objectMapper = new ObjectMapper();

    @Autowired
    MongoClient mongoClient;

    /**
     * 处理指定目录下的文本文件，仅对未入库的段落进行向量化并保存。
     */
    public String processTextFiles(){
        java.net.URL url = this.getClass().getProtectionDomain().getCodeSource().getLocation();
        String jarPath = null;
        try {
            jarPath = java.net.URLDecoder.decode(url.getFile(), "UTF-8");
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        }

        // 如果是以 .jar 结尾，则表示是从 JAR 包运行的
        if (jarPath.endsWith(".jar")) {
            // 取到 JAR 文件所在目录
            jarPath = new java.io.File(jarPath).getParentFile().getAbsolutePath();
        } else {
            // 否则是开发环境（IDE）运行，返回项目根目录或其他默认路径
            jarPath = new java.io.File("").getAbsolutePath();
        }

        System.out.println(jarPath);

        String baseDirectory = jarPath + "\\text_files\\knowledge_library";
        String processedDirectory = jarPath + "\\text_files\\put_here";

        File dir = null;
        File processedDir = null;
        try {
            dir = new File(ResourceUtils.getURL(baseDirectory).getFile());
            processedDir = new File(ResourceUtils.getURL(processedDirectory).getFile());
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        }

        // 确保 dir 文件夹存在
        if (!dir.exists()) {
            boolean created = dir.mkdirs();
            if (!created) {
                log.error("Failed to create dir directory");
            }
        }

        // 确保 processed 文件夹存在
        if (!processedDir.exists()) {
            boolean created = processedDir.mkdirs();
            if (!created) {
                log.error("Failed to create processed directory");
            }
        }

        IndexMapping indexMapping = IndexMapping.builder()
                .dimension(1024)
                .metadataFieldNames(new HashSet<>())
                .build();

        MongoDbEmbeddingStore embeddingStore = MongoDbEmbeddingStore.builder()
                .databaseName("search")
                .collectionName("langchaintest")
                .createIndex(true)
                .indexName("vector_index")
                .indexMapping(indexMapping)
                .fromClient(mongoClient)
                .build();

        // 获取所有待处理的 .txt 文件
        List<File> txtFiles = FileUtil.loopFiles(dir, file -> StrUtil.endWith(file.getName(), ".txt"));

        for (File file : txtFiles) {
            String content = FileUtil.readString(file, StandardCharsets.UTF_8);

            Document document = Document.from(content, Metadata.from("documentName", file.getName()));
            DocumentByParagraphSplitter splitter = new DocumentByParagraphSplitter(60, 30);
            List<dev.langchain4j.data.segment.TextSegment> segments = splitter.split(document);

            for (dev.langchain4j.data.segment.TextSegment segment : segments) {
                // 构建查询请求
                Embedding queryEmbedding = new Embedding(getVectorRepresentation(segment.text()));
                EmbeddingSearchRequest searchRequest = EmbeddingSearchRequest.builder()
                        .queryEmbedding(queryEmbedding)
                        .maxResults(1)
                        .build();

                EmbeddingSearchResult<TextSegment> result = embeddingStore.search(searchRequest);
                List<EmbeddingMatch<TextSegment>> matches = result.matches();

                // 如果没有匹配项，说明这个段落还没入库
                if (matches.isEmpty()) {
                    embeddingStore.add(queryEmbedding, segment);
                    log.info("✅ 新增段落到数据库: {}", segment.text().substring(0, Math.min(50, segment.text().length())) + "...");
                } else {
                    log.info("🔁 段落已存在，跳过处理: {}", segment.text().substring(0, Math.min(50, segment.text().length())) + "...");
                }
            }

            // 将文件移动到 processed 文件夹
            File targetFile = new File(processedDir, file.getName());
            if (file.renameTo(targetFile)) {
                log.info("📦 文件 '{}' 已移动至 processed 文件夹", file.getName());
            } else {
                log.warn("⚠️ 文件 '{}' 移动失败，请检查权限或目标路径", file.getName());
            }
        }
        return "✅ 已处理所有文件";
    }

    public List<String> queryVectorDatabase(String query, int maxResults) {
        IndexMapping indexMapping = IndexMapping.builder()
                .dimension(1024)
                .metadataFieldNames(new HashSet<>())
                .build();
        MongoDbEmbeddingStore embeddingStore = MongoDbEmbeddingStore.builder()
                .databaseName("search")
                .collectionName("langchaintest")
                .createIndex(true)
                .indexName("vector_index")
                .indexMapping(indexMapping)
                .fromClient(mongoClient)
                .build();

        Embedding queryEmbedding = new Embedding(getVectorRepresentation(query));

        EmbeddingSearchRequest searchRequest = EmbeddingSearchRequest.builder()
                .queryEmbedding(queryEmbedding)
                .maxResults(maxResults)
                .build();

        EmbeddingSearchResult<TextSegment> searchResult = embeddingStore.search(searchRequest);
        List<EmbeddingMatch<TextSegment>> matches = searchResult.matches();

        // 构建返回的字符串列表，仅包含文本内容
        List<String> result = matches.stream()
                .map(match -> match.embedded().text())  // 只取文本内容
                .toList();  // 或者使用 .collect(Collectors.toList()) 如果你使用的Java版本不支持 .toList()

        // 如果你也想打印出来，保留打印逻辑
        for (EmbeddingMatch<TextSegment> embeddingMatch : matches) {
            log.info("Response: " + embeddingMatch.embedded().text());
            log.info("Score: " + embeddingMatch.score());
        }

        return result;
    }

    // 使用 HTTP + Jackson 获取 Embedding 向量
    // 调用多模态嵌入API获取向量表示
    private float[] getVectorRepresentation(String text){
        // 构建请求体
        var requestBody = new EmbeddingRequestDto();
        requestBody.setModel("multimodal-embedding-v1");

        // 创建内容列表
        List<ContentDto> contents = new ArrayList<>();
        contents.add(new ContentDto(text, null, null));
        requestBody.setInput(new EmbeddingInputDto(contents));

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("Authorization", "Bearer " + apiKey); // 请确保使用正确的授权令牌

        HttpEntity<EmbeddingRequestDto> entity = new HttpEntity<>(requestBody, headers);

        ResponseEntity<String> response = restTemplate.postForEntity(
                "https://dashscope.aliyuncs.com/api/v1/services/embeddings/multimodal-embedding/multimodal-embedding",
                entity,
                String.class
        );

        if (response.getStatusCode() != HttpStatus.OK) {
            throw new RuntimeException("Failed to call embedding API: " + response.getBody());
        }

        // 使用 Jackson 解析响应
        JsonNode rootNode = null;
        try {
            rootNode = objectMapper.readTree(response.getBody());
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
        JsonNode embeddingListNode = rootNode.path("output").path("embeddings");

        if (embeddingListNode.isMissingNode()) {
            throw new RuntimeException("Invalid response format: missing 'text_embedding'");
        }

        List<Float> embeddingList = new ArrayList<>();
        for (JsonNode node : embeddingListNode) {
            JsonNode embeddingArray = node.path("embedding");
            if (!embeddingArray.isArray()) {
                throw new RuntimeException("Expected an array for 'embedding'");
            }
            for (JsonNode embeddingValue : embeddingArray) {
                embeddingList.add(embeddingValue.floatValue());
            }
        }

// 转为 float[]
        float[] embedding = new float[embeddingList.size()];
        for (int i = 0; i < embedding.length; i++) {
            embedding[i] = embeddingList.get(i);
        }

        return embedding;
    }

    // 请求 DTO
    private static class EmbeddingRequestDto {
        private String model;
        private EmbeddingInputDto input;

        // Getters and Setters
        public String getModel() { return model; }
        public void setModel(String model) { this.model = model; }
        public EmbeddingInputDto getInput() { return input; }
        public void setInput(EmbeddingInputDto input) { this.input = input; }
    }

    private static class EmbeddingInputDto {
        private List<ContentDto> contents;

        public EmbeddingInputDto(List<ContentDto> contents) {
            this.contents = contents;
        }

        // Getter
        public List<ContentDto> getContents() { return contents; }
    }

    @Data
    private static class ContentDto {
        private String text;
        private String image;
        private String video;

        public ContentDto(String text, String image, String video) {
            this.text = text;
            this.image = image;
            this.video = video;
        }
    }
}