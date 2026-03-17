package io.github.mangomaner.mangobot.agent.model.dto;

import io.github.mangomaner.mangobot.agent.model.enums.SessionSource;
import lombok.Data;

import java.util.List;

@Data
public class UpdateMcpToolsEnabledListRequest {
    private List<SessionSource> enabledList;
}
