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
@TableName("illness")
public class Illness {

    @TableId(value = "illnessid", type = IdType.AUTO)
    private Integer illnessid;

    @TableField("cowtown")
    private String cowtown;

    @TableField("illnessname")
    private String illnessname;

    @TableField("illnessfeature")
    private String illnessfeature;

    @TableField("illnesspic")
    private String illnesspic;

    @TableField("xueweicount")
    private String xueweicount;

    @TableField("toolscount")
    private String toolscount;

    @TableField("xuewei1")
    private String xuewei1;

    @TableField("xuewei2")
    private String xuewei2;

    @TableField("xuewei3")
    private String xuewei3;

    @TableField("xuewei4")
    private String xuewei4;

    @TableField("xuewei5")
    private String xuewei5;

    @TableField("tools1")
    private String tools1;

    @TableField("tools2")
    private String tools2;

    @TableField("tools3")
    private String tools3;

    @TableField("tools4")
    private String tools4;

    @TableField("illnessbadgename")
    private String illnessbadgename;

    @TableField("illnessbadgepath")
    private String illnessbadgepath;
}

