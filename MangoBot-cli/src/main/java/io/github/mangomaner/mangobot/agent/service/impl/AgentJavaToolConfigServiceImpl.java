package io.github.mangomaner.mangobot.agent.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import io.github.mangomaner.mangobot.agent.model.domain.AgentJavaToolConfig;
import io.github.mangomaner.mangobot.agent.service.AgentJavaToolConfigService;
import io.github.mangomaner.mangobot.mapper.agent.AgentJavaToolConfigMapper;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AgentJavaToolConfigServiceImpl extends ServiceImpl<AgentJavaToolConfigMapper, AgentJavaToolConfig>
    implements AgentJavaToolConfigService{

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
}
