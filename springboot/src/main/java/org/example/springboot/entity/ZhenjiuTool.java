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
@TableName("zhenjiutools")
public class ZhenjiuTool {

    @TableId(value = "toolsid", type = IdType.AUTO)
    private Integer toolsId;

    @TableField("toolsname")
    private String toolsName;

    @TableField("toolspic1")
    private String toolsPic1;

    @TableField("toolspic2")
    private String toolsPic2;

    @TableField("toolspic3")
    private String toolsPic3;

    @TableField("toolsbrief")
    private String toolsBrief;

    @TableField("toolstitle1")
    private String toolsTitle1;

    @TableField("toolstitle2")
    private String toolsTitle2;

    @TableField("toolstitle3")
    private String toolsTitle3;

    @TableField("toolstext1")
    private String toolsText1;

    @TableField("toolstext2")
    private String toolsText2;

    @TableField("toolstext3")
    private String toolsText3;

    @TableField("skillid")
    private Integer skillId;
}
