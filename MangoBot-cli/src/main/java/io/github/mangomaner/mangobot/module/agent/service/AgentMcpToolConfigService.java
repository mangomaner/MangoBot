package io.github.mangomaner.mangobot.module.agent.service;

import io.github.mangomaner.mangobot.module.agent.model.domain.AgentMcpToolConfig;
import io.github.mangomaner.mangobot.module.agent.model.enums.SessionSource;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

public interface AgentMcpToolConfigService extends IService<AgentMcpToolConfig> {

    List<AgentMcpToolConfig> listByMcpConfigId(Integer mcpConfigId);

    List<String> listEnabledToolNamesByMcpConfigId(Integer mcpConfigId);

    void deleteByMcpConfigId(Integer mcpConfigId);

    void deleteByMcpConfigIdAndToolName(Integer mcpConfigId, String toolName);

    /**
     * 更新 MCP 工具的启用来源列表
     *
     * @param id          工具ID
     * @param enabledList 启用的来源列表
     */
    void updateEnabledList(Integer id, List<SessionSource> enabledList);

    /**
     * 批量更新某个 MCP 连接下所有工具的启用来源列表
     *
     * @param mcpConfigId MCP 连接ID
     * @param enabledList 启用的来源列表
     */
    void updateEnabledListByMcpConfigId(Integer mcpConfigId, List<SessionSource> enabledList);
}
