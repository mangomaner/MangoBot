package org.mango.mangobot.knowledgeLibrary.service;

import cn.hutool.core.io.FileUtil;
import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch._types.KnnSearch;
import co.elastic.clients.elasticsearch.core.*;
import co.elastic.clients.elasticsearch.core.knn_search.KnnSearchQuery;
import co.elastic.clients.elasticsearch.core.search.Hit;
import co.elastic.clients.elasticsearch.core.search.SourceConfig;
import co.elastic.clients.elasticsearch.core.search.SourceFilter;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.Resource;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.mango.mangobot.knowledgeLibrary.utils.FileUtils;
import org.mango.mangobot.knowledgeLibrary.utils.VectorUtil;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Value;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Slf4j
@AllArgsConstructor
public class EsTextProcessingService {

    @Resource
    private ElasticsearchClient elasticsearchClient;
    @Resource
    private ObjectMapper objectMapper;

    private static final String INDEX_NAME = "knowledge_segments";
    private static final double SIMILARITY_THRESHOLD = 0.99;

    public String processTextFiles() {
        String jarPath = getJarPath();
        String baseDirectory = jarPath + "\\text_files\\put_here";
        String processedDirectory = jarPath + "\\text_files\\knowledge_library";

        File dir = FileUtils.ensureDirectoryExists(baseDirectory);
        File processedDir = FileUtils.ensureDirectoryExists(processedDirectory);

        List<File> txtFiles = FileUtil.loopFiles(dir, file -> file.getName().endsWith(".txt"));

        for (File file : txtFiles) {
            String content = FileUtil.readString(file, StandardCharsets.UTF_8);
            List<String> segments = VectorUtil.splitByParagraph(content, 10);

            StringBuilder fileContentBuilder = new StringBuilder();

            for (String segment : segments) {
                List<Float> embedding = getVectorRepresentation(segment);

                // 搜索相似段落
                boolean exists = searchSimilarEmbedding(embedding, segment);

                if (!exists) {
                    saveToElasticsearch(segment, embedding);
                    fileContentBuilder.append(segment).append("\n");
                    log.info("新增段落到ES: {}", segment.substring(0, Math.min(50, segment.length())) + "...");
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

    public String processTextContent(String content) {
        List<String> segments = VectorUtil.splitByParagraph(content, 10);
        StringBuilder fileContentBuilder = new StringBuilder();

        for (String segment : segments) {
            List<Float> embedding = getVectorRepresentation(segment);
            boolean exists = searchSimilarEmbedding(embedding, segment);

            if (!exists) {
                saveToElasticsearch(segment, embedding);
                fileContentBuilder.append(segment).append("\n");
                log.info("新增段落到ES: {}", segment.substring(0, Math.min(50, segment.length())) + "...");
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
        List<Float> queryEmbedding = getVectorRepresentation(query);

        try {
            // 创建 KNN 查询部分
            KnnSearch knnSearch = KnnSearch.of(b -> b
                    .field("embedding")
                    .queryVector(queryEmbedding)
                    .numCandidates(100L)
                    .k(1L)
            );

            // 构建 source filter 只获取 content 字段
            SourceConfig sourceConfig = SourceConfig.of(b -> b
                    .filter(SourceFilter.of(f -> f
                            .includes(Arrays.asList("content"))
                    ))
            );

            // 构建完整的 SearchRequest
            SearchRequest request = SearchRequest.of(b -> b
                    .index(INDEX_NAME)
                    .knn(knnSearch)  // 注意这里是 KnnSearch 而不是 KnnSearchQuery
                    .source(sourceConfig)
            );

            SearchResponse<Map> response = elasticsearchClient.search(request, Map.class);
            List<Hit<Map>> hits = response.hits().hits();

            List<String> results = new ArrayList<>();
            for (Hit<Map> hit : hits) {
                Map<String, Object> source = hit.source();
                if (source != null && source.containsKey("content")) {
                    String content = source.get("content").toString();
                    double score = hit.score();
                    log.info("匹配内容: {}, 相似度: {}", content, score);
                    results.add(content);
                }
            }

            return results;

        } catch (IOException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("Elasticsearch 查询失败", e);
        }
    }

    private void saveToElasticsearch(String content, List<Float> embedding) {
        try {
            String id = UUID.randomUUID().toString(); // 可替换为 hash 避免重复插入

            IndexRequest<Map<String, Object>> request = IndexRequest.of(b -> b
                    .index(INDEX_NAME)
                    .id(id)
                    .document(Map.of(
                            "content", content,
                            "embedding", embedding
                    ))
            );

            elasticsearchClient.index(request);
        } catch (IOException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("保存到 Elasticsearch 失败", e);
        }
    }

    private boolean searchSimilarEmbedding(List<Float> embedding, String segment) {
        try {
            // 创建 KNN 查询部分
            KnnSearch knnSearch = KnnSearch.of(b -> b
                    .field("embedding")
                    .queryVector(embedding)
                    .numCandidates(100L)
                    .k(1L)
            );

            // 构建 source filter 只获取 content 字段
            SourceConfig sourceConfig = SourceConfig.of(b -> b
                    .filter(SourceFilter.of(f -> f
                            .includes(Arrays.asList("content"))
                    ))
            );

            // 构建完整的 SearchRequest
            SearchRequest request = SearchRequest.of(b -> b
                    .index(INDEX_NAME)
                    .knn(knnSearch)  // 注意这里是 KnnSearch 而不是 KnnSearchQuery
                    .source(sourceConfig)
            );

            SearchResponse<Map> response = elasticsearchClient.search(request, Map.class);
            List<Hit<Map>> hits = response.hits().hits();

            if (!hits.isEmpty()) {
                Hit<Map> hit = hits.get(0);
                double score = hit.score();
                if (score > SIMILARITY_THRESHOLD) {
                    return true; // 存在高度相似的段落
                }
            }
            return false;
        } catch (IOException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("搜索相似段落失败", e);
        }
    }

    private String getJarPath() {
        java.net.URL url = this.getClass().getProtectionDomain().getCodeSource().getLocation();
        String jarPath = null;
        try {
            jarPath = java.net.URLDecoder.decode(url.getFile(), "UTF-8");
            if (jarPath.endsWith(".jar")) {
                jarPath = new java.io.File(jarPath).getParentFile().getAbsolutePath();
            } else {
                jarPath = new java.io.File("").getAbsolutePath();
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return jarPath;
    }

    // TODO: 实现你的 getVectorRepresentation 方法
    private List<Float> getVectorRepresentation(String text) {
        // 调用模型或 mock 返回向量
        return Collections.emptyList();
    }
}