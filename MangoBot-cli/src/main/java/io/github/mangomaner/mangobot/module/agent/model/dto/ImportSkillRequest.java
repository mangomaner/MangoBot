package io.github.mangomaner.mangobot.module.agent.model.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 导入单文件 Skill（SKILL.md）请求
 */
@Data
@Schema(description = "导入单文件 Skill 请求")
public class ImportSkillRequest {

    @NotBlank(message = "技能名称不能为空")
    @Schema(description = "技能名称", requiredMode = Schema.RequiredMode.REQUIRED)
    private String skillName;

    @Schema(description = "技能描述（content 无 frontmatter 时使用）")
    private String description;

    @NotBlank(message = "SKILL.md 内容不能为空")
    @Schema(description = "SKILL.md 内容（可含 YAML frontmatter）", requiredMode = Schema.RequiredMode.REQUIRED)
    private String content;
}
