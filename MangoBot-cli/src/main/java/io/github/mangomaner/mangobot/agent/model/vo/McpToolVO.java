package io.github.mangomaner.mangobot.agent.model.vo;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.mangomaner.mangobot.agent.model.domain.AgentMcpToolConfig;
import io.github.mangomaner.mangobot.agent.model.enums.SessionSource;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;

import java.util.Collections;
import java.util.List;

@Data
@Slf4j
public class McpToolVO {
    private Integer id;
    private Integer mcpConfigId;
    private String toolName;
    private String description;
    private String inputSchema;
    private Boolean enabled;
    private List<SessionSource> enabledList;
    private List<SessionSource> availableList;

    private static final ObjectMapper objectMapper = new ObjectMapper();

    public static McpToolVO from(AgentMcpToolConfig config) {
        McpToolVO vo = new McpToolVO();
        BeanUtils.copyProperties(config, vo);
        
        // 解析 JSON 字符串为 List<SessionSource>
        vo.setEnabledList(parseSourceList(config.getEnabledList()));
        vo.setAvailableList(parseSourceList(config.getAvailableList()));
        
        return vo;
    }

    /**
     * 解析来源列表 JSON 字符串
     */
    private static List<SessionSource> parseSourceList(String json) {
        if (json == null || json.isEmpty()) {
            return Collections.emptyList();
        }
        try {
            List<String> sourceKeys = objectMapper.readValue(json, new TypeReference<List<String>>() {});
            return sourceKeys.stream()
                    .map(SessionSource::fromKey)
                    .filter(source -> source != null)
                    .toList();
        } catch (Exception e) {
            log.warn("Failed to parse source list: {}", json, e);
            return Collections.emptyList();
        }
    }
}
