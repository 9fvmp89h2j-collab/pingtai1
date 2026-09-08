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

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("traingame")
@Schema(description = "调车小游戏关卡配置")
public class TrainGame {

    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    @TableField("jingluoname")
    private String jingluoName;

    @TableField("game1")
    private String game1;

    @TableField("game2")
    private String game2;

    @TableField("game3")
    private String game3;

    @TableField("game4")
    private String game4;

    @TableField("game5")
    private String game5;

    @TableField("game1brief")
    private String game1Brief;

    @TableField("game2brief")
    private String game2Brief;

    @TableField("game3brief")
    private String game3Brief;

    @TableField("game4brief")
    private String game4Brief;

    @TableField("game5brief")
    private String game5Brief;
}
