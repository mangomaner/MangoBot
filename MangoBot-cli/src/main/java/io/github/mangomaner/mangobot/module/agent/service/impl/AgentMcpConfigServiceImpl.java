package io.github.mangomaner.mangobot.module.agent.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import io.github.mangomaner.mangobot.module.agent.model.domain.AgentMcpConfig;
import io.github.mangomaner.mangobot.module.agent.service.AgentMcpConfigService;
import io.github.mangomaner.mangobot.system.mapper.agent.AgentMcpConfigMapper;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AgentMcpConfigServiceImpl extends ServiceImpl<AgentMcpConfigMapper, AgentMcpConfig>
    implements AgentMcpConfigService{

    @Override
    public List<AgentMcpConfig> listEnabled() {
        return list(new LambdaQueryWrapper<AgentMcpConfig>()
                .eq(AgentMcpConfig::getEnabled, true));
    }

    @Override
    public void updateConnectionStatus(Integer id, Integer status) {
        update(new LambdaUpdateWrapper<AgentMcpConfig>()
                .eq(AgentMcpConfig::getId, id)
                .set(AgentMcpConfig::getConnectionStatus, status));
    }
}
