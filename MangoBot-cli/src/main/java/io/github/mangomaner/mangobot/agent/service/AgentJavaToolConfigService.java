package io.github.mangomaner.mangobot.agent.service;

import io.github.mangomaner.mangobot.agent.model.domain.AgentJavaToolConfig;
import io.github.mangomaner.mangobot.agent.model.enums.SessionSource;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

public interface AgentJavaToolConfigService extends IService<AgentJavaToolConfig> {

    AgentJavaToolConfig getByClassName(String className);

    List<AgentJavaToolConfig> listEnabled();

    void deleteByClassName(String className);

    void updateMetadata(Integer id, String toolName, String description, String category);

    /**
     * 更新工具的启用来源列表
     *
     * @param id          工具ID
     * @param enabledList 启用的来源列表
     */
    void updateEnabledList(Integer id, List<SessionSource> enabledList);
}
