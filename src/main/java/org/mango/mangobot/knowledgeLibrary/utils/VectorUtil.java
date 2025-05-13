package org.mango.mangobot.knowledgeLibrary.utils;

import java.util.ArrayList;
import java.util.List;

public class VectorUtil {

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

            // 判断是否应该结束当前段落（遇到空行 or 达到最大长度）
            if (trimmedLine.isEmpty() || (currentParagraph.length() >= minParagraphLength && !trimmedLine.isEmpty())) {
                if (currentParagraph.length() > 0) {
                    paragraphs.add(currentParagraph.toString());
                    currentParagraph.setLength(0); // 清空缓存
                }
            }
        }

        // 添加最后一个段落（如果有剩余）
        if (currentParagraph.length() > 0) {
            paragraphs.add(currentParagraph.toString());
        }

        return paragraphs;
    }
}
