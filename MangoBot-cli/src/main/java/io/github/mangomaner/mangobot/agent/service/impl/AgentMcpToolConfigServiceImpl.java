package io.github.mangomaner.mangobot.agent.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import io.github.mangomaner.mangobot.agent.model.domain.AgentMcpToolConfig;
import io.github.mangomaner.mangobot.agent.service.AgentMcpToolConfigService;
import io.github.mangomaner.mangobot.mapper.agent.AgentMcpToolConfigMapper;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class AgentMcpToolConfigServiceImpl extends ServiceImpl<AgentMcpToolConfigMapper, AgentMcpToolConfig>
    implements AgentMcpToolConfigService{

    @Override
    public List<AgentMcpToolConfig> listByMcpConfigId(Integer mcpConfigId) {
        return list(new LambdaQueryWrapper<AgentMcpToolConfig>()
                .eq(AgentMcpToolConfig::getMcpConfigId, mcpConfigId));
    }

    @Override
    public List<String> listEnabledToolNamesByMcpConfigId(Integer mcpConfigId) {
        return list(new LambdaQueryWrapper<AgentMcpToolConfig>()
                .eq(AgentMcpToolConfig::getMcpConfigId, mcpConfigId)
                .eq(AgentMcpToolConfig::getEnabled, true))
                .stream()
                .map(AgentMcpToolConfig::getToolName)
                .collect(Collectors.toList());
    }

    @Override
    public void deleteByMcpConfigId(Integer mcpConfigId) {
        remove(new LambdaQueryWrapper<AgentMcpToolConfig>()
                .eq(AgentMcpToolConfig::getMcpConfigId, mcpConfigId));
    }

    @Override
    public void deleteByMcpConfigIdAndToolName(Integer mcpConfigId, String toolName) {
        remove(new LambdaQueryWrapper<AgentMcpToolConfig>()
                .eq(AgentMcpToolConfig::getMcpConfigId, mcpConfigId)
                .eq(AgentMcpToolConfig::getToolName, toolName));
    }
}
