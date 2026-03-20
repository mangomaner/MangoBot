package io.github.mangomaner.mangobot.api.context.state;

import java.util.HashMap;
import java.util.Map;

public class ToolExecuteState {

    private final Map<String, Integer> toolExecuteCount = new HashMap<>();

    public int getToolExecuteCount(String toolName) {
        return toolExecuteCount.getOrDefault(toolName, 0);
    }
    public void addToolExecuteCount(String toolName) {
        int times = toolExecuteCount.getOrDefault(toolName, 0);
        toolExecuteCount.put(toolName, times + 1);
    }
}
