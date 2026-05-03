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
 * 针灸起源故事（originstory 表）
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("originstory")
@Schema(description = "针灸起源故事")
public class OriginStory {

    /** 数据库列名为 originstoryID */
    @TableId(value = "originstoryID", type = IdType.AUTO)
    @Schema(description = "主键")
    private Long id;

    @TableField("storytitle")
    @Schema(description = "故事标题")
    private String storyTitle;

    @TableField("storysubtitle")
    @Schema(description = "副标题")
    private String storySubtitle;

    @TableField("storytext")
    @Schema(description = "正文")
    private String storyText;

    @TableField("storypic1")
    @Schema(description = "配图1")
    private String storyPic1;

    @TableField("storypic2")
    @Schema(description = "配图2")
    private String storyPic2;

    @TableField("storypic3")
    @Schema(description = "配图3")
    private String storyPic3;

    @TableField("media")
    @Schema(description = "媒体文件（视频等）")
    private String media;

    @TableField("skillid")
    @Schema(description = "关联技能ID")
    private Long skillId;
}
