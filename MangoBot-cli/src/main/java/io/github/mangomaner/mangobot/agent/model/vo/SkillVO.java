package io.github.mangomaner.mangobot.agent.model.vo;

import io.github.mangomaner.mangobot.agent.capability.skill.SkillManager;
import io.github.mangomaner.mangobot.agent.model.domain.AgentSkillConfig;
import lombok.Data;
import org.springframework.beans.BeanUtils;

import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Skill VO
 * 
 * @see AgentSkillConfig
 * @see SkillManager
 */
@Data
public class SkillVO {
    private Integer id;
    private String skillName;
    private String description;
    private String skillPath;
    private String boundToolIds;
    private Boolean enabled;
    private Boolean available;

    public static SkillVO from(AgentSkillConfig config, SkillManager skillManager) {
        SkillVO vo = new SkillVO();
        BeanUtils.copyProperties(config, vo);
        Path skillMdPath = skillManager.getSkillsDirectory()
            .resolve(config.getSkillPath())
            .resolve("SKILL.md");
        vo.setAvailable(Files.exists(skillMdPath));
        return vo;
    }
}
