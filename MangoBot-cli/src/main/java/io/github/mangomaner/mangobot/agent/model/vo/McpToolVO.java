package io.github.mangomaner.mangobot.agent.model.vo;

import io.github.mangomaner.mangobot.agent.model.domain.AgentMcpToolConfig;
import lombok.Data;
import org.springframework.beans.BeanUtils;

@Data
public class McpToolVO {
    private Integer id;
    private Integer mcpConfigId;
    private String toolName;
    private String description;
    private String inputSchema;
    private Boolean enabled;

    public static McpToolVO from(AgentMcpToolConfig config) {
        McpToolVO vo = new McpToolVO();
        BeanUtils.copyProperties(config, vo);
        return vo;
    }
}
