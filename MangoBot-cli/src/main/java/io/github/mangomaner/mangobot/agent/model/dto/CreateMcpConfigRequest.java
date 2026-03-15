package io.github.mangomaner.mangobot.agent.model.dto;

import lombok.Data;

@Data
public class CreateMcpConfigRequest {
    private String mcpName;
    private String transportType;
    private String connectionConfig;
}
