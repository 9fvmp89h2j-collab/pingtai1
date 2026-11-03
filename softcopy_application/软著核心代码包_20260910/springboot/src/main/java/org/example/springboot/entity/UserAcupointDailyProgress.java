package org.example.springboot.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Builder;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("user_acupoint_daily_progress")
public class UserAcupointDailyProgress {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long userId;
    private LocalDate caseDate;
    private String acupointCode;
}
