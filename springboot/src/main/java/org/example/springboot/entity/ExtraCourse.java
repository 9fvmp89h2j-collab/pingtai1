package org.example.springboot.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("extracourse")
public class ExtraCourse {

    @TableId(value = "extracourseid", type = IdType.AUTO)
    private Integer extraCourseId;

    @TableField("extracoursename")
    private String extraCourseName;

    @TableField("extracoursebrief")
    private String extraCourseBrief;

    @TableField("extracoursedes")
    private String extraCourseDes;

    @TableField("extracourseicon")
    private String extraCourseIcon;

    @TableField("extracoursepic1")
    private String extraCoursePic1;

    @TableField("extracoursepic2")
    private String extraCoursePic2;

    @TableField("extracoursepic3")
    private String extraCoursePic3;

    @TableField("skillid")
    private Integer skillId;
}

