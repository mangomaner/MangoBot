package org.mango.mangobot.knowledgeLibrary.service;

import cn.hutool.core.io.FileUtil;
import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch.core.*;
import co.elastic.clients.elasticsearch.core.search.Hit;
import co.elastic.clients.elasticsearch.core.search.SourceConfig;
import co.elastic.clients.elasticsearch.core.search.SourceFilter;
import jakarta.annotation.Resource;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.mango.mangobot.knowledgeLibrary.utils.FileUtils;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.*;

import static org.mango.mangobot.utils.VectorUtil.splitByParagraph;

@Service
@Slf4j
@AllArgsConstructor
public class EsTextProcessingService {

    @Resource
    private ElasticsearchClient elasticsearchClient;

    private static final String INDEX_NAME = "knowledge_library";

    public String processTextFiles() {
        String jarPath = getJarPath();
        String baseDirectory = jarPath + "\\text_files\\put_here";
        String processedDirectory = jarPath + "\\text_files\\knowledge_library";

        File dir = FileUtils.ensureDirectoryExists(baseDirectory);
        File processedDir = FileUtils.ensureDirectoryExists(processedDirectory);

        List<File> txtFiles = FileUtil.loopFiles(dir, file -> file.getName().endsWith(".txt"));

        for (File file : txtFiles) {
            String content = FileUtil.readString(file, StandardCharsets.UTF_8);
            List<String> segments = splitByParagraph(content, 5000, "123"); // 按段落分

            StringBuilder fileContentBuilder = new StringBuilder();

            for (String segment : segments) {
                String hashId = sha256Hash(segment);

                boolean exists = checkDocumentExists(hashId);

                if (!exists) {
                    saveToElasticsearch(segment, hashId);
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

    public String processTextContent(String content, String keyWord, Integer maxLength) {
        List<String> segments = splitByParagraph(content, maxLength, keyWord);
        StringBuilder fileContentBuilder = new StringBuilder();

        for (String segment : segments) {
            String hashId = sha256Hash(segment);
            boolean exists = checkDocumentExists(hashId);

            if (!exists) {
                saveToElasticsearch(segment, hashId);
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


        String outputFilePath = processedDirectory + File.separator + keyWord + System.currentTimeMillis() + ".txt";
        // 防止文件格式不合规
        try {
            FileUtils.writeToFile(fileContentBuilder.toString(), outputFilePath);
        } finally {
            FileUtils.writeToFile(fileContentBuilder.toString(), processedDirectory + File.separator + System.currentTimeMillis() + ".txt");
        }


        return "已处理所有段落并写入文件：" + outputFilePath;
    }

    public List<String> queryVectorDatabase(String query, int maxResults) {
        try {
            SearchRequest request = SearchRequest.of(b -> b
                    .index(INDEX_NAME)
                    .query(q -> q
                            .match(m -> m
                                    .field("content")
                                    .query(query)
                            )
                    )
                    .source(SourceConfig.of(b1 -> b1
                            .filter(SourceFilter.of(f -> f
                                    .includes(Collections.singletonList("content"))
                            ))
                    ))
                    .size(maxResults)
            );

            SearchResponse<Map> response = elasticsearchClient.search(request, Map.class);
            List<Hit<Map>> hits = response.hits().hits();

            List<String> results = new ArrayList<>();
            for (Hit<Map> hit : hits) {
                Map<String, Object> source = hit.source();
                if (source != null && source.containsKey("content")) {
                    String content = source.get("content").toString();
                    double score = hit.score();
                    log.info("匹配内容: {}, 相似度评分: {}", content, score);
                    results.add(content);
                }
            }

            return results;

        } catch (IOException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("Elasticsearch 查询失败", e);
        }
    }

    private void saveToElasticsearch(String content, String id) {
        try {
            IndexRequest<Map<String, Object>> request = IndexRequest.of(b -> b
                    .index(INDEX_NAME)
                    .id(id)
                    .document(Map.of(
                            "content", content
                    ))
            );

            elasticsearchClient.index(request);
        } catch (IOException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("保存到 Elasticsearch 失败", e);
        }
    }

    private boolean checkDocumentExists(String id) {
        try {
            GetRequest getRequest = GetRequest.of(b -> b
                    .index(INDEX_NAME)
                    .id(id)
            );

            GetResponse<Map> response = elasticsearchClient.get(getRequest, Map.class);
            return response.found();
        } catch (IOException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("检查文档是否存在失败", e);
        }
    }

    private String sha256Hash(String input) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hashBytes = digest.digest(input.getBytes(StandardCharsets.UTF_8));
            StringBuilder hexString = new StringBuilder();

            for (byte b : hashBytes) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) {
                    hexString.append('0');
                }
                hexString.append(hex);
            }

            return hexString.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("SHA-256 算法不可用", e);
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

}