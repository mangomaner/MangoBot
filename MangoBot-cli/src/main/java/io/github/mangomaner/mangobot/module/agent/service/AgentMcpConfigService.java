package io.github.mangomaner.mangobot.module.agent.service;

import io.github.mangomaner.mangobot.module.agent.model.domain.AgentMcpConfig;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

public interface AgentMcpConfigService extends IService<AgentMcpConfig> {

    List<AgentMcpConfig> listEnabled();

    void updateConnectionStatus(Integer id, Integer status);
}
