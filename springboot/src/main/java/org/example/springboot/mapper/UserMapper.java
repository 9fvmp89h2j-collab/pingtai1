package org.example.springboot.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.example.springboot.entity.User;

import java.time.LocalDate;
import java.util.List;

/**
 * 用户数据访问层
 * @author system
 */
@Mapper
public interface UserMapper extends BaseMapper<User> {
    // 继承BaseMapper，获得基础的CRUD操作
    // 所有复杂查询都在Service层使用Lambda构造器实现

    @Select("${sql}")
    Long selectCountByRawSql(@Param("sql") String sql);

    @Select("SELECT COUNT(1) FROM user_checkin WHERE user_id = #{userId} AND checkin_date = #{day}")
    int countCheckinByDay(@Param("userId") Long userId, @Param("day") LocalDate day);

    @Select("SELECT COUNT(1) FROM user_checkin WHERE user_id = #{userId}")
    long countCheckinsByUserId(@Param("userId") Long userId);

    @Select("SELECT MAX(checkin_date) FROM user_checkin WHERE user_id = #{userId}")
    LocalDate findLastCheckinDate(@Param("userId") Long userId);

    @Insert("INSERT INTO user_checkin(user_id, checkin_date, created_at) VALUES(#{userId}, #{day}, NOW())")
    int insertCheckin(@Param("userId") Long userId, @Param("day") LocalDate day);

    @Select("SELECT checkin_date FROM user_checkin WHERE user_id = #{userId} AND checkin_date >= #{start} AND checkin_date <= #{end} ORDER BY checkin_date ASC")
    List<LocalDate> listCheckinDates(@Param("userId") Long userId, @Param("start") LocalDate start, @Param("end") LocalDate end);

    @Insert("INSERT INTO user_game_reward(user_id, game_code, reward_date, score_awarded, created_at) " +
            "VALUES(#{userId}, #{gameCode}, #{rewardDate}, #{scoreAwarded}, NOW())")
    int insertGameReward(
            @Param("userId") Long userId,
            @Param("gameCode") String gameCode,
            @Param("rewardDate") LocalDate rewardDate,
            @Param("scoreAwarded") Integer scoreAwarded
    );
}
