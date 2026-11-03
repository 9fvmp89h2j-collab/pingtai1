package org.example.springboot.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.example.springboot.entity.UserAcupointDailyProgress;

import java.time.LocalDate;
import java.util.List;

@Mapper
public interface UserAcupointDailyProgressMapper extends BaseMapper<UserAcupointDailyProgress> {
    @Insert("INSERT IGNORE INTO user_acupoint_daily_progress (user_id, case_date, acupoint_code) " +
            "VALUES (#{userId}, #{caseDate}, #{code})")
    int insertIgnore(@Param("userId") Long userId,
                     @Param("caseDate") LocalDate caseDate,
                     @Param("code") String code);

    @Select("SELECT acupoint_code FROM user_acupoint_daily_progress " +
            "WHERE user_id = #{userId} AND case_date = #{caseDate}")
    List<String> selectCodes(@Param("userId") Long userId,
                             @Param("caseDate") LocalDate caseDate);

    @Select("SELECT DISTINCT acupoint_code FROM user_acupoint_daily_progress WHERE user_id = #{userId}")
    List<String> selectDistinctCodes(@Param("userId") Long userId);
}
