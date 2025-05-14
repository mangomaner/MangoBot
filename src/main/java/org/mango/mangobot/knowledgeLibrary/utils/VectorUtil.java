package org.mango.mangobot.knowledgeLibrary.utils;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

public class VectorUtil {

    // 支持的句子结束符（英文句号、中文句号、问号、感叹号）
    private static final Pattern SENTENCE_END_PATTERN = Pattern.compile("[。\\.]");
    // 常见缩写（如 Mr., Dr.）中的句号不作为分割符
    private static final String ABBREVIATION_PATTERN = "\\b[A-Za-z][a-z]{1,3}\\.";

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
}
