package io.github.mangomaner.mangobot.agent.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.mangomaner.mangobot.agent.model.domain.AgentJavaToolConfig;
import io.github.mangomaner.mangobot.agent.model.enums.SessionSource;
import io.github.mangomaner.mangobot.agent.service.AgentJavaToolConfigService;
import io.github.mangomaner.mangobot.mapper.agent.AgentJavaToolConfigMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
public class AgentJavaToolConfigServiceImpl extends ServiceImpl<AgentJavaToolConfigMapper, AgentJavaToolConfig>
    implements AgentJavaToolConfigService{

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public AgentJavaToolConfig getByClassName(String className) {
        return getOne(new LambdaQueryWrapper<AgentJavaToolConfig>()
                .eq(AgentJavaToolConfig::getClassName, className));
    }

    @Override
    public List<AgentJavaToolConfig> listEnabled() {
        return list(new LambdaQueryWrapper<AgentJavaToolConfig>()
                .eq(AgentJavaToolConfig::getEnabled, true));
    }

    @Override
    public void deleteByClassName(String className) {
        remove(new LambdaQueryWrapper<AgentJavaToolConfig>()
                .eq(AgentJavaToolConfig::getClassName, className));
    }

    @Override
    public void updateMetadata(Integer id, String toolName, String description, String category) {
        AgentJavaToolConfig config = new AgentJavaToolConfig();
        config.setId(id);
        config.setToolName(toolName);
        config.setDescription(description);
        config.setCategory(category);
        updateById(config);
    }

    @Override
    public void updateEnabledList(Integer id, List<SessionSource> enabledList) {
        AgentJavaToolConfig config = getById(id);
        if (config == null) {
            throw new IllegalArgumentException("Tool not found: " + id);
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
            log.info("Updated enabled list for tool {}: {}", id, enabledListJson);
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
}
