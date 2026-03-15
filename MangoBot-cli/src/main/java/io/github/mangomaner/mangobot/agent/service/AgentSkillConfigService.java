package io.github.mangomaner.mangobot.agent.service;

import io.github.mangomaner.mangobot.agent.model.domain.AgentSkillConfig;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

public interface AgentSkillConfigService extends IService<AgentSkillConfig> {

    AgentSkillConfig getBySkillPath(String skillPath);

    List<AgentSkillConfig> listEnabled();
}
