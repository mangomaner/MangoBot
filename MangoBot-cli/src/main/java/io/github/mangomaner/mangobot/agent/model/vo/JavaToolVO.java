package io.github.mangomaner.mangobot.agent.model.vo;

import io.github.mangomaner.mangobot.agent.capability.tool.JavaToolLoader;
import io.github.mangomaner.mangobot.agent.model.domain.AgentJavaToolConfig;
import lombok.Data;
import org.springframework.beans.BeanUtils;

/**
 * Java 工具 VO
 * 
 * @see AgentJavaToolConfig
 * @see JavaToolLoader
 */
@Data
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

    public static JavaToolVO from(AgentJavaToolConfig config, JavaToolLoader loader) {
        JavaToolVO vo = new JavaToolVO();
        BeanUtils.copyProperties(config, vo);
        vo.setAvailable(loader.isClassLoadable(config.getClassName()));
        return vo;
    }
}
