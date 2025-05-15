package org.mango.mangobot.utils;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.Resource;
import lombok.Data;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

/**
 * 两个工具：
 *      1. 获取向量表示
 *      2. 文本拆分（按照段落拆分，若超过限制大小则取最后一个句号作为结尾）
 */
@Component
public class VectorUtil {

    @Resource
    private RestTemplate restTemplate;
    @Resource
    private ObjectMapper objectMapper;
    @Value("${chat-model.api-key}")
    private String apiKey;

    // 支持的句子结束符（英文句号、中文句号、问号、感叹号）
    private static final Pattern SENTENCE_END_PATTERN = Pattern.compile("[。\\.]");

    // 使用 HTTP + Jackson 获取 Embedding 向量
    // 调用多模态嵌入API获取向量表示
    public List<Float> getVectorRepresentation(String text){
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

        ResponseEntity<String> response = null;
        try{
            response = restTemplate.postForEntity(
                    "https://dashscope.aliyuncs.com/api/v1/services/embeddings/multimodal-embedding/multimodal-embedding",
                    entity,
                    String.class
            );
        } catch (Exception e) {
            e.printStackTrace();
        }

        if(response == null) return new ArrayList<>();

//        if (response.getStatusCode() != HttpStatus.OK) {
//            throw new RuntimeException("Failed to call embedding API: " + response.getBody());
//        }

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
        return embeddingList;
    }

    /**
     * 将输入文本按段落拆分为 List<String> 并在每个段落末尾添加关键词
     *
     * @param content 输入文本内容
     * @param minParagraphLength 最小段落长度（防止空行或无效段落）
     * @param keyWord 要添加到每个段落末尾的关键词
     * @return 段落列表
     */
    public static List<String> splitByParagraph(String content, int minParagraphLength, String keyWord) {
        List<String> paragraphs = new ArrayList<>();

        // 使用正则表达式按两个及以上换行符分割为段落
        String[] rawParagraphs = content.split("[\\r\\n,\\n\\n]");

        for (String rawParagraph : rawParagraphs) {
            String trimmedParagraph = rawParagraph.trim();
            if (!trimmedParagraph.isEmpty() && trimmedParagraph.length() >= minParagraphLength) {
                // 分割长段落
                List<String> splitResult = splitLongParagraph(trimmedParagraph, 500);
                // 添加关键词并加入结果列表
                for (String part : splitResult) {
                    if(!isStructuredData(part)) {
                        paragraphs.add(part + " " + keyWord);
                    }
                }
            }
        }

        return paragraphs;
    }
    private static boolean isStructuredData(String text) {
        if (text == null || text.isEmpty()) {
            return false;
        }

        // 快速过滤：如果段落中没有结构化数据的关键符号，直接返回 false
        if (text.indexOf('•') == -1) {
            return false;
        }

        // 统计结构化数据的字符数（通过关键符号密度估算）
        int structuredCharCount = 0;
        int totalLength = text.length();

        // 统计 "•" 的数量
        int bulletCount = countOccurrences(text, '•');

        // 假设每个 "•" 代表一个结构化数据单元
        structuredCharCount = (bulletCount) * 8; // 估算每个单元的平均长度

        // 如果结构化数据占比 > 80%，判定为无效段落
        return (double) structuredCharCount / totalLength > 0.8;
    }

    // 辅助方法：统计字符出现次数
    private static int countOccurrences(String text, char target) {
        int count = 0;
        for (int i = 0; i < text.length(); i++) {
            if (text.charAt(i) == target) {
                count++;
            }
        }
        return count;
    }
    /**
     * 递归分割长段落
     *
     * @param paragraph 需要分割的段落
     * @param maxLength 最大允许长度
     * @return 分割后的段落列表
     */
    private static List<String> splitLongParagraph(String paragraph, int maxLength) {
        List<String> result = new ArrayList<>();
        if (paragraph == null || paragraph.length() <= maxLength) {
            result.add(paragraph);
            return result;
        }

        // 查找最后一个句子结束符的位置
        int splitIndex = findLastSentenceEnd(paragraph, maxLength);
        if (splitIndex != -1) {
            String firstPart = paragraph.substring(0, splitIndex + 1).trim();
            String remaining = paragraph.substring(splitIndex + 1).trim();

            result.add(firstPart);
            result.addAll(splitLongParagraph(remaining, maxLength));
        } else {
            // 找不到合适的句子结尾，直接截断
            result.add(paragraph.substring(0, maxLength));
            result.addAll(splitLongParagraph(paragraph.substring(maxLength), maxLength));
        }

        return result;
    }

    /**
     * 查找最后一个句子结束符的位置（简单版）
     *
     * @param text      输入文本
     * @param maxLength 最大允许长度
     * @return 句子结束符索引，-1 表示未找到
     */
    private static int findLastSentenceEnd(String text, int maxLength) {
        int maxIndex = Math.min(text.length(), maxLength);
        for (int i = maxIndex - 1; i >= 0; i--) {
            char c = text.charAt(i);
            if (SENTENCE_END_PATTERN.matcher(String.valueOf(c)).matches()) {
                return i;
            }
        }
        return -1;
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
