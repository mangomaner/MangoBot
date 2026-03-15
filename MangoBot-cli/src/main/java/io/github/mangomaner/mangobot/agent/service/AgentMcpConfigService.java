package io.github.mangomaner.mangobot.agent.service;

import io.github.mangomaner.mangobot.agent.model.domain.AgentMcpConfig;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

public interface AgentMcpConfigService extends IService<AgentMcpConfig> {

    List<AgentMcpConfig> listEnabled();

    void updateConnectionStatus(Integer id, Integer status);
}
