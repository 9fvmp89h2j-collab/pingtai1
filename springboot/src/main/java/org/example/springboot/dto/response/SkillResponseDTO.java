package org.example.springboot.dto.response;

import lombok.Data;

@Data
public class SkillResponseDTO {
    private Integer skillId;
    private String skillName;
    private String skillBriefDescription;
    private String skillDescription;
    private String skillPic;
    private String skillCategory;
    private String skillScore;
    private String skillType;
}

