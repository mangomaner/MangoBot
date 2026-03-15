package io.github.mangomaner.mangobot.agent.tools;

import io.agentscope.core.tool.Tool;
import io.agentscope.core.tool.ToolParam;
import io.github.mangomaner.mangobot.annotation.MangoTool;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@MangoTool(name = "DateTimeTool", description = "日期时间工具，用于获取和格式化日期时间", category = "SYSTEM")
public class DateTimeTool {

    @Tool(description = "获取当前日期时间")
    public String getCurrentDateTime() {
        return LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
    }

    @Tool(description = "获取当前日期")
    public String getCurrentDate() {
        return LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
    }

    @Tool(description = "获取当前时间")
    public String getCurrentTime() {
        return LocalDateTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss"));
    }

    @Tool(description = "格式化日期时间")
    public String formatDateTime(
            @ToolParam(name = "pattern", description = "日期时间格式，如 yyyy-MM-dd HH:mm:ss") String pattern) {
        try {
            return LocalDateTime.now().format(DateTimeFormatter.ofPattern(pattern));
        } catch (Exception e) {
            return "格式错误: " + e.getMessage();
        }
    }
}
