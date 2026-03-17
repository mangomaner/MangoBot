package io.github.mangomaner.mangobot.agent.model.vo;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.mangomaner.mangobot.agent.capability.tool.JavaToolLoader;
import io.github.mangomaner.mangobot.agent.model.domain.AgentJavaToolConfig;
import io.github.mangomaner.mangobot.agent.model.enums.SessionSource;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;

import java.util.Collections;
import java.util.List;

/**
 * Java 工具 VO
 * 
 * @see AgentJavaToolConfig
 * @see JavaToolLoader
 */
@Data
@Slf4j
public class JavaToolVO {
    private Integer id;
    private String className;
    private String constructorArgs;
    private String loadType;
    private String toolName;
    private String description;
    private String category;
    private Integer pluginId;
    private Boolean enabled;
    private Boolean available;
    private List<SessionSource> enabledList;
    private List<SessionSource> availableList;

    private static final ObjectMapper objectMapper = new ObjectMapper();

    public static JavaToolVO from(AgentJavaToolConfig config, JavaToolLoader loader) {
        JavaToolVO vo = new JavaToolVO();
        BeanUtils.copyProperties(config, vo);
        vo.setAvailable(loader.isClassLoadable(config.getClassName()));
        
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
