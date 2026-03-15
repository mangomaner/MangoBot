package io.github.mangomaner.mangobot.agent.service;

import io.github.mangomaner.mangobot.agent.model.domain.AgentJavaToolConfig;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

public interface AgentJavaToolConfigService extends IService<AgentJavaToolConfig> {

    AgentJavaToolConfig getByClassName(String className);

    List<AgentJavaToolConfig> listEnabled();

    void deleteByClassName(String className);

    void updateMetadata(Integer id, String toolName, String description, String category);
}
