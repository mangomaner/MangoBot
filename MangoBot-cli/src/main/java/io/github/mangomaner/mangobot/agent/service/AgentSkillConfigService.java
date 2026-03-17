package io.github.mangomaner.mangobot.agent.service;

import io.github.mangomaner.mangobot.agent.model.domain.AgentSkillConfig;
import io.github.mangomaner.mangobot.agent.model.enums.SessionSource;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

public interface AgentSkillConfigService extends IService<AgentSkillConfig> {

    AgentSkillConfig getBySkillPath(String skillPath);

    List<AgentSkillConfig> listEnabled();

    /**
     * 更新 Skill 的启用来源列表
     *
     * @param id          Skill ID
     * @param enabledList 启用的来源列表
     */
    void updateEnabledList(Integer id, List<SessionSource> enabledList);
}
