package org.example.springboot.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 技能表 skills
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("skills")
@Schema(description = "技能")
public class Skill {

    @TableId(value = "skillid", type = IdType.INPUT)
    @Schema(description = "技能ID")
    private Integer skillId;

    @TableField("skillname")
    private String skillName;

    @TableField("skillbriefdescription")
    private String skillBriefDescription;

    @TableField("skilldescription")
    private String skillDescription;

    @TableField("skillpic")
    private String skillPic;

    @TableField("skillcategory")
    private String skillCategory;

    @TableField("skillscore")
    private String skillScore;

    @TableField("skilltype")
    private String skillType;
}
