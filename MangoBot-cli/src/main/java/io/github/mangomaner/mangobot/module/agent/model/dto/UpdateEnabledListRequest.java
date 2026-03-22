package io.github.mangomaner.mangobot.module.agent.model.dto;

import io.github.mangomaner.mangobot.module.agent.model.enums.SessionSource;
import lombok.Data;

import java.util.List;

/**
 * 更新启用列表请求
 * <p>
 * 用于更新 Java 工具、MCP 工具或 Skill 的启用来源列表。
 * enabledList 的内容必须全部在 availableList 之中。
 */
@Data
public class UpdateEnabledListRequest {

    /**
     * 启用的来源列表
     * <p>
     * 必须是 availableList 的子集
     */
    private List<SessionSource> enabledList;
}
