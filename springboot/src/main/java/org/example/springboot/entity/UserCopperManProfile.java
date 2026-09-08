package org.example.springboot.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDate;

@Data
@TableName("user_copper_man_profile")
public class UserCopperManProfile {
    @TableId(value = "user_id", type = IdType.INPUT)
    private Long userId;
    private Integer copperTokens;
    private Integer starSand;
    private Integer completedCases;
    private LocalDate lastCompletedCaseDate;
}
