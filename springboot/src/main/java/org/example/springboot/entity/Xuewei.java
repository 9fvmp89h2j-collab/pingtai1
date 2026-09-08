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
 * 穴位表 xuewei
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("xuewei")
@Schema(description = "穴位")
public class Xuewei {

    @TableId(value = "xueweiid", type = IdType.AUTO)
    @Schema(description = "主键")
    private Long xueweiId;

    @TableField("xueweiname")
    @Schema(description = "穴位名称")
    private String xueweiName;

    /** 库表字段名为 xueweicatagory（拼写同库） */
    @TableField("xueweicatagory")
    @Schema(description = "经络分类")
    private String xueweiCatagory;

    /** MySQL 保留字 position，需反引号 */
    @TableField("`position`")
    @Schema(description = "定位")
    private String position;

    @TableField("illness")
    @Schema(description = "功效/主治病症")
    private String illness;

    @TableField("xueweipic1")
    @Schema(description = "穴位图")
    private String xueweiPic1;

    /**
     * 关联 skills.skillid，用于弹窗「采集」。
     */
    @TableField("skillid")
    private Integer skillId;
}
