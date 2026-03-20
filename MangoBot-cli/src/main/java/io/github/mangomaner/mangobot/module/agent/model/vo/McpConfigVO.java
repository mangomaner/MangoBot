package io.github.mangomaner.mangobot.module.agent.model.vo;

import io.github.mangomaner.mangobot.module.agent.model.domain.AgentMcpConfig;
import lombok.Data;
import org.springframework.beans.BeanUtils;

@Data
public class McpConfigVO {
    private Integer id;
    private String mcpName;
    private String transportType;
    private String connectionConfig;
    private Integer connectionStatus;
    private Boolean enabled;

    public static McpConfigVO from(AgentMcpConfig config) {
        McpConfigVO vo = new McpConfigVO();
        BeanUtils.copyProperties(config, vo);
        return vo;
    }
}
