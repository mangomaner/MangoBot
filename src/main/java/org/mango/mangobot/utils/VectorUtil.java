package org.mango.mangobot.utils;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.Resource;
import lombok.Data;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
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
     * 将输入文本按段落拆分为 List<String>
     *
     * @param content 输入文本内容
     * @param minParagraphLength 最小段落长度（防止空行或无效段落）
     * @return 段落列表
     */
    public static List<String> splitByParagraph(String content, int minParagraphLength) {
        List<String> paragraphs = new ArrayList<>();
        String[] lines = content.split("[\\r\\n,\\n\\n]"); // 支持 Windows 和 Linux 换行符

        StringBuilder currentParagraph = new StringBuilder();
        for (String line : lines) {
            String trimmedLine = line.trim();

            if (!trimmedLine.isEmpty()) {
                if (currentParagraph.length() > 0) {
                    currentParagraph.append("\n");
                }
                currentParagraph.append(line);
            }

            // 判断是否应该结束当前段落（遇到空行 or 达到最小长度）
            if (trimmedLine.isEmpty() || (currentParagraph.length() >= minParagraphLength && !trimmedLine.isEmpty())) {
                if (currentParagraph.length() > 0) {
                    String paragraphContent = currentParagraph.toString();
                    // 递归分割段落
                    List<String> splitResult = splitLongParagraph(paragraphContent, 500);
                    paragraphs.addAll(splitResult);
                    currentParagraph.setLength(0); // 清空缓存
                }
            }
        }

        // 添加最后一个段落（如果有剩余）
        if (currentParagraph.length() > 0) {
            String finalContent = currentParagraph.toString();
            List<String> splitResult = splitLongParagraph(finalContent, 500);
            paragraphs.addAll(splitResult);
        }

        return paragraphs;
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
        if (paragraph == null || paragraph.isEmpty() || paragraph.length() <= maxLength) {
            result.add(paragraph);
            return result;
        }

        // 查找最后一个句子结束符的位置（排除缩写）
        int splitIndex = findLastSentenceEnd(paragraph, maxLength);
        if (splitIndex != -1) {
            String firstPart = paragraph.substring(0, splitIndex + 1).trim();
            String remaining = paragraph.substring(splitIndex + 1).trim();
            if (!firstPart.isEmpty()) {
                result.add(firstPart);
            }
            result.addAll(splitLongParagraph(remaining, maxLength)); // 递归处理剩余部分
        } else {
            // 无法找到合适分割点，直接截断
            result.add(paragraph.substring(0, maxLength));
            result.addAll(splitLongParagraph(paragraph.substring(maxLength), maxLength));
        }

        return result;
    }

    /**
     * 查找最后一个句子结束符的位置（排除缩写）
     *
     * @param text      输入文本
     * @param maxLength 最大允许长度
     * @return 句子结束符索引，-1 表示未找到
     */
    private static int findLastSentenceEnd(String text, int maxLength) {
        int maxIndex = Math.min(text.length(), maxLength);
        for (int i = maxIndex; i >= 0; i--) {
            char c = text.charAt(i);
            if (SENTENCE_END_PATTERN.matcher(String.valueOf(c)).matches()) {
                // 检查是否是缩写（如 Mr., Dr.）
                if (i > 0 && Character.isLetter(text.charAt(i - 1))) {
                    // 检查前面是否有字母（如 Mr. 中的 r）
                    int j = i - 2;
                    while (j >= 0 && Character.isLetter(text.charAt(j))) {
                        j--;
                    }
                    if (j >= 0 && Character.isWhitespace(text.charAt(j))) {
                        // 是句子结束符
                        return i;
                    }
                } else {
                    // 是句子结束符
                    return i;
                }
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
