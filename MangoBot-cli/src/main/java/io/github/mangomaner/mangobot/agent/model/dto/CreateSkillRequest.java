package io.github.mangomaner.mangobot.agent.model.dto;

import lombok.Data;

import java.util.List;

@Data
public class CreateSkillRequest {
    private String skillName;
    private String description;
    private String skillPath;
    private String skillContent;
    private List<Integer> boundToolIds;
}
