package io.github.mangomaner.mangobot.module.agent.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.mangomaner.mangobot.module.agent.model.domain.AgentMcpToolConfig;
import io.github.mangomaner.mangobot.module.agent.model.enums.SessionSource;
import io.github.mangomaner.mangobot.module.agent.service.AgentMcpToolConfigService;
import io.github.mangomaner.mangobot.system.mapper.agent.AgentMcpToolConfigMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
public class AgentMcpToolConfigServiceImpl extends ServiceImpl<AgentMcpToolConfigMapper, AgentMcpToolConfig>
    implements AgentMcpToolConfigService{

    private final ObjectMapper objectMapper = new ObjectMapper();

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

    @Override
    public void updateEnabledList(Integer id, List<SessionSource> enabledList) {
        AgentMcpToolConfig config = getById(id);
        if (config == null) {
            throw new IllegalArgumentException("MCP tool not found: " + id);
        }

        // 验证 enabledList 是 availableList 的子集
        List<String> availableSources = parseSourceList(config.getAvailableList());
        List<String> enabledSources = enabledList.stream()
                .map(SessionSource::getSourceKey)
                .toList();

        if (!availableSources.containsAll(enabledSources)) {
            throw new IllegalArgumentException("enabledList must be a subset of availableList");
        }

        try {
            String enabledListJson = objectMapper.writeValueAsString(enabledSources);
            config.setEnabledList(enabledListJson);
            updateById(config);
            log.info("Updated enabled list for MCP tool {}: {}", id, enabledListJson);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Failed to serialize enabled list", e);
        }
    }

    private List<String> parseSourceList(String json) {
        if (json == null || json.isEmpty()) {
            return List.of();
        }
        try {
            return objectMapper.readValue(json, List.class);
        } catch (JsonProcessingException e) {
            log.warn("Failed to parse source list: {}", json, e);
            return List.of();
        }
    }

    @Override
    public void updateEnabledListByMcpConfigId(Integer mcpConfigId, List<SessionSource> enabledList) {
        List<AgentMcpToolConfig> tools = listByMcpConfigId(mcpConfigId);
        if (tools.isEmpty()) {
            return;
        }

        List<String> enabledSources = enabledList.stream()
                .map(SessionSource::getSourceKey)
                .toList();

        try {
            String enabledListJson = objectMapper.writeValueAsString(enabledSources);
            for (AgentMcpToolConfig tool : tools) {
                List<String> availableSources = parseSourceList(tool.getAvailableList());
                List<String> validEnabledSources = enabledSources.stream()
                        .filter(availableSources::contains)
                        .collect(Collectors.toList());
                
                String finalEnabledListJson = objectMapper.writeValueAsString(validEnabledSources);
                tool.setEnabledList(finalEnabledListJson);
            }
            updateBatchById(tools);
            log.info("Updated enabled list for all tools under MCP {}: {}", mcpConfigId, enabledListJson);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Failed to serialize enabled list", e);
        }
    }
}
