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
 * 经络表 jingluo
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("jingluo")
@Schema(description = "经络")
public class Jingluo {

    @TableId(value = "jingluoid", type = IdType.AUTO)
    @Schema(description = "主键")
    private Integer jingluoId;

    @TableField("jingluoname")
    @Schema(description = "经络名称")
    private String jingluoName;

    /** 库表字段名为 jingluocatagory（拼写同库） */
    @TableField("jingluocatagory")
    @Schema(description = "经络分类")
    private String jingluoCatagory;

    @TableField("jingluoorder")
    @Schema(description = "循行/次序（对应腧穴页「定位」）")
    private String jingluoOrder;

    @TableField("illness")
    @Schema(description = "功效/主治病症")
    private String illness;

    @TableField("jingluopic")
    @Schema(description = "经络图")
    private String jingluoPic;

    @TableField("skillid")
    private Integer skillId;
}
