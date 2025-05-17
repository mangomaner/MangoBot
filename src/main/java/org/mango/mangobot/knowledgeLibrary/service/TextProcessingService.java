package org.mango.mangobot.knowledgeLibrary.service;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.StrUtil;
import com.mongodb.client.MongoClient;
import dev.langchain4j.data.embedding.Embedding;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.store.embedding.EmbeddingMatch;
import dev.langchain4j.store.embedding.EmbeddingSearchRequest;
import dev.langchain4j.store.embedding.EmbeddingSearchResult;
import dev.langchain4j.store.embedding.mongodb.IndexMapping;
import dev.langchain4j.store.embedding.mongodb.MongoDbEmbeddingStore;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.mango.mangobot.knowledgeLibrary.utils.FileUtils;
import org.mango.mangobot.utils.VectorUtil;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Paths;
import java.util.HashSet;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
public class TextProcessingService {

    @Resource
    private VectorUtil vectorUtil;
    @Resource
    MongoClient mongoClient;

    /**
     * 处理指定目录下的文本文件，仅对未入库的段落进行向量化并保存。
     */
    public String processTextFiles() {
        String jarPath = getJarPath();
        String baseDirectory = jarPath + "\\text_files\\put_here";
        String processedDirectory = jarPath + "\\text_files\\knowledge_library";

        File dir = FileUtils.ensureDirectoryExists(baseDirectory);
        File processedDir = FileUtils.ensureDirectoryExists(processedDirectory);

        MongoDbEmbeddingStore embeddingStore = buildEmbeddingStore();

        List<File> txtFiles = FileUtil.loopFiles(dir, file -> StrUtil.endWith(file.getName(), ".txt"));

        for (File file : txtFiles) {
            String content = FileUtil.readString(file, StandardCharsets.UTF_8);
            List<String> segments = VectorUtil.splitByParagraph(content, 5000, "");

            StringBuilder fileContentBuilder = new StringBuilder();

            for (String segment : segments) {
                Embedding queryEmbedding = new Embedding(convert(vectorUtil.getVectorRepresentation(segment)));
                EmbeddingSearchRequest searchRequest = EmbeddingSearchRequest.builder()
                        .queryEmbedding(queryEmbedding)
                        .minScore(0.99)
                        .maxResults(1)
                        .build();

                EmbeddingSearchResult<TextSegment> result = embeddingStore.search(searchRequest);
                List<EmbeddingMatch<TextSegment>> matches = result.matches();

                if (matches.isEmpty()) {
                    embeddingStore.add(segment, queryEmbedding);
                    fileContentBuilder.append(segment).append("\n");
                    log.info("新增段落到数据库: {}", segment.substring(0, Math.min(50, segment.length())) + "...");
                } else {
                    fileContentBuilder.append(segment).append("\n");
                    log.info("段落已存在，跳过处理: {}", segment.substring(0, Math.min(50, segment.length())) + "...");
                }
            }

            // 将处理后的内容写入 processed 目录
            String outputFilePath = Paths.get(processedDirectory, file.getName()).toString();
            FileUtils.writeToFile(fileContentBuilder.toString(), outputFilePath);
            if (file.delete()) {
                log.info("原文件 '{}' 已删除", file.getName());
            } else {
                log.warn("无法删除原文件 '{}'", file.getName());
            }
        }

        return "已处理所有文件";
    }

    /**
     * 处理传入的文本内容，仅对未入库的段落进行向量化并保存，并将结果写入到txt文件。
     */
    public String processTextContent(String content) {
        MongoDbEmbeddingStore embeddingStore = buildEmbeddingStore();

        List<String> segments = VectorUtil.splitByParagraph(content, 5000, "");
        StringBuilder fileContentBuilder = new StringBuilder();

        for (String segment : segments) {
            Embedding queryEmbedding = new Embedding(convert(vectorUtil.getVectorRepresentation(segment)));
            EmbeddingSearchRequest searchRequest = EmbeddingSearchRequest.builder()
                    .queryEmbedding(queryEmbedding)
                    .minScore(0.99)
                    .maxResults(1)
                    .build();

            EmbeddingSearchResult<TextSegment> result = embeddingStore.search(searchRequest);
            List<EmbeddingMatch<TextSegment>> matches = result.matches();

            if (matches.isEmpty()) {
                embeddingStore.add(segment, queryEmbedding);
                fileContentBuilder.append(segment).append("\n");
                log.info("新增段落到数据库: {}", segment.substring(0, Math.min(50, segment.length())) + "...");
            } else {
                fileContentBuilder.append(segment).append("\n");
                log.info("段落已存在，跳过处理: {}", segment.substring(0, Math.min(50, segment.length())) + "...");
            }
        }

        // 写入文件
        String jarPath = getJarPath();
        String processedDirectory = jarPath + "\\text_files\\knowledge_library";
        File dir = FileUtils.ensureDirectoryExists(processedDirectory);

        String outputFilePath = processedDirectory + File.separator + "output_" + System.currentTimeMillis() + ".txt";
        FileUtils.writeToFile(fileContentBuilder.toString(), outputFilePath);

        return "已处理所有段落并写入文件：" + outputFilePath;
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

        Embedding queryEmbedding = new Embedding(convert(vectorUtil.getVectorRepresentation(query)));

        EmbeddingSearchRequest searchRequest = EmbeddingSearchRequest.builder()
                .queryEmbedding(queryEmbedding)
                .maxResults(maxResults)
                .build();

        EmbeddingSearchResult<TextSegment> searchResult = embeddingStore.search(searchRequest);
        List<EmbeddingMatch<TextSegment>> matches = searchResult.matches();

        // 构建返回的字符串列表，仅包含文本内容
        List<String> result = matches.stream()
                .map(match -> match.embeddingId())  // 只取文本内容
                .collect(Collectors.toList());  // 或者使用 .collect(Collectors.toList()) 如果你使用的Java版本不支持 .toList()

        // 保留打印逻辑
        for (EmbeddingMatch<TextSegment> embeddingMatch : matches) {
            log.info("Response: " + embeddingMatch.embeddingId());
            log.info("Score: " + embeddingMatch.score());
        }

        return result;
    }

    private MongoDbEmbeddingStore buildEmbeddingStore() {
        IndexMapping indexMapping = IndexMapping.builder()
                .dimension(1024)
                .metadataFieldNames(new HashSet<>())
                .build();

        return MongoDbEmbeddingStore.builder()
                .databaseName("search")
                .collectionName("langchaintest")
                .createIndex(true)
                .indexName("vector_index")
                .indexMapping(indexMapping)
                .fromClient(mongoClient)
                .build();
    }

    private String getJarPath() {
        java.net.URL url = this.getClass().getProtectionDomain().getCodeSource().getLocation();
        String jarPath = null;
        try {
            jarPath = java.net.URLDecoder.decode(url.getFile(), "UTF-8");
            // 如果是以 .jar 结尾，则表示是从 JAR 包运行的
            if (jarPath.endsWith(".jar")) {
                // 取到 JAR 文件所在目录
                jarPath = new java.io.File(jarPath).getParentFile().getAbsolutePath();
            } else {
                // 否则是开发环境（IDE）运行，返回项目根目录或其他默认路径
                jarPath = new java.io.File("").getAbsolutePath();
            }
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        }
        return jarPath;
    }

    private float[] convert(List<Float> list) {
        if (list == null || list.isEmpty()) {
            return new float[0];
        }

        float[] array = new float[list.size()];
        for (int i = 0; i < list.size(); i++) {
            array[i] = list.get(i); // 自动拆箱 Float -> float
        }
        return array;
    }
}