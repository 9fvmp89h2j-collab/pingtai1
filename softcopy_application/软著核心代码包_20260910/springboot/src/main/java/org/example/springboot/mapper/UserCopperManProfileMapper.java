package org.example.springboot.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Update;
import org.example.springboot.entity.UserCopperManProfile;

import java.time.LocalDate;

@Mapper
public interface UserCopperManProfileMapper extends BaseMapper<UserCopperManProfile> {
    @Insert("INSERT IGNORE INTO user_copper_man_profile " +
            "(user_id, copper_tokens, star_sand, completed_cases) VALUES (#{userId}, 0, 0, 0)")
    int ensureProfile(@Param("userId") Long userId);

    @Update("UPDATE user_copper_man_profile SET copper_tokens = copper_tokens + 2, " +
            "star_sand = star_sand + 5, completed_cases = completed_cases + 1, " +
            "last_completed_case_date = #{caseDate} WHERE user_id = #{userId} " +
            "AND (last_completed_case_date IS NULL OR last_completed_case_date < #{caseDate})")
    int awardDailyCase(@Param("userId") Long userId,
                       @Param("caseDate") LocalDate caseDate);
}
