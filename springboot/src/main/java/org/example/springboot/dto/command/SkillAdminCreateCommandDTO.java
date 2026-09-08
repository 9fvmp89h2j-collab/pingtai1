package org.example.springboot.dto.command;

import lombok.Data;

@Data
public class SkillAdminCreateCommandDTO {
    private Integer skillId;
    private String skillName;
    private String skillBriefDescription;
    private String skillDescription;
    private String skillPic;
    private String skillCategory;
    private String skillScore;
    private String skillType;
}

