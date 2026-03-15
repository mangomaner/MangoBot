package io.github.mangomaner.mangobot.agent.model.dto;

import lombok.Data;

import java.util.List;

@Data
public class UpdateSkillRequest {
    private String skillName;
    private String description;
    private String skillContent;
    private List<Integer> boundToolIds;
}
