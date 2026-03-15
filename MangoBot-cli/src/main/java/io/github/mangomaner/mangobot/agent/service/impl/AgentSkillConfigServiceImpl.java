package io.github.mangomaner.mangobot.agent.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import io.github.mangomaner.mangobot.agent.model.domain.AgentSkillConfig;
import io.github.mangomaner.mangobot.agent.service.AgentSkillConfigService;
import io.github.mangomaner.mangobot.mapper.agent.AgentSkillConfigMapper;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AgentSkillConfigServiceImpl extends ServiceImpl<AgentSkillConfigMapper, AgentSkillConfig>
    implements AgentSkillConfigService{

    @Override
    public AgentSkillConfig getBySkillPath(String skillPath) {
        return getOne(new LambdaQueryWrapper<AgentSkillConfig>()
                .eq(AgentSkillConfig::getSkillPath, skillPath));
    }

    @Override
    public List<AgentSkillConfig> listEnabled() {
        return list(new LambdaQueryWrapper<AgentSkillConfig>()
                .eq(AgentSkillConfig::getEnabled, true));
    }
}
