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
 * 针灸名医故事（doctorstory 表）
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("doctorstory")
@Schema(description = "针灸名医故事")
public class DoctorStory {

    /** 与库表列 doctorid 对应（勿写成 id） */
    @TableId(value = "doctorid", type = IdType.AUTO)
    @Schema(description = "主键")
    private Long id;

    @TableField("doctorname")
    @Schema(description = "名医名字")
    private String doctorName;

    @TableField("doctorbrief")
    @Schema(description = "名医简介")
    private String doctorBrief;

    @TableField("doctordetail")
    @Schema(description = "名医详解")
    private String doctorDetail;

    @TableField("doctorpic1")
    @Schema(description = "配图1")
    private String doctorPic1;

    @TableField("doctorpic2")
    @Schema(description = "配图2")
    private String doctorPic2;

    @TableField("doctorpic3")
    @Schema(description = "配图3")
    private String doctorPic3;

    @TableField("media")
    @Schema(description = "媒体文件（视频等）")
    private String media;

    @TableField("previewpic")
    @Schema(description = "故事预览封面")
    private String previewPic;

    @TableField("readingglossary")
    @Schema(description = "重点词词库JSON")
    private String readingGlossaryJson;

    @TableField("skillid")
    @Schema(description = "关联技能ID（skills.skillid）")
    private Integer skillId;
}
