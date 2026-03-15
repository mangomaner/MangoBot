package io.github.mangomaner.mangobot.agent.tools;

import io.agentscope.core.tool.Tool;
import io.agentscope.core.tool.ToolParam;
import io.github.mangomaner.mangobot.annotation.MangoTool;

import java.util.UUID;

@MangoTool(name = "TextTool", description = "文本处理工具，用于处理和转换文本", category = "SYSTEM")
public class TextTool {

    @Tool(description = "将文本转换为大写")
    public String toUpperCase(@ToolParam(name = "text", description = "要转换的文本") String text) {
        return text.toUpperCase();
    }

    @Tool(description = "将文本转换为小写")
    public String toLowerCase(@ToolParam(name = "text", description = "要转换的文本") String text) {
        return text.toLowerCase();
    }

    @Tool(description = "获取文本长度")
    public int length(@ToolParam(name = "text", description = "要计算长度的文本") String text) {
        return text.length();
    }

    @Tool(description = "截取文本子串")
    public String substring(
            @ToolParam(name = "text", description = "原始文本") String text,
            @ToolParam(name = "start", description = "起始位置（从0开始）") int start,
            @ToolParam(name = "end", description = "结束位置") int end) {
        if (start < 0 || end > text.length() || start > end) {
            throw new IllegalArgumentException("无效的截取范围");
        }
        return text.substring(start, end);
    }

    @Tool(description = "替换文本中的内容")
    public String replace(
            @ToolParam(name = "text", description = "原始文本") String text,
            @ToolParam(name = "target", description = "要替换的内容") String target,
            @ToolParam(name = "replacement", description = "替换后的内容") String replacement) {
        return text.replace(target, replacement);
    }

    @Tool(description = "检查文本是否包含指定内容")
    public boolean contains(
            @ToolParam(name = "text", description = "原始文本") String text,
            @ToolParam(name = "search", description = "要搜索的内容") String search) {
        return text.contains(search);
    }

    @Tool(description = "去除文本首尾空白")
    public String trim(@ToolParam(name = "text", description = "要处理的文本") String text) {
        return text.trim();
    }

    @Tool(description = "生成 UUID")
    public String generateUuid() {
        return UUID.randomUUID().toString();
    }
}
