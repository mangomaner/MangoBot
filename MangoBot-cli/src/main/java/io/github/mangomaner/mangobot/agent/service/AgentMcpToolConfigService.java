package io.github.mangomaner.mangobot.agent.service;

import io.github.mangomaner.mangobot.agent.model.domain.AgentMcpToolConfig;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

public interface AgentMcpToolConfigService extends IService<AgentMcpToolConfig> {

    List<AgentMcpToolConfig> listByMcpConfigId(Integer mcpConfigId);

    List<String> listEnabledToolNamesByMcpConfigId(Integer mcpConfigId);

    void deleteByMcpConfigId(Integer mcpConfigId);

    void deleteByMcpConfigIdAndToolName(Integer mcpConfigId, String toolName);
}
