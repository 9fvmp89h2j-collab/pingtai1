package org.example.springboot.mapper;

import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;
import org.example.springboot.dto.response.BadgeResponseDTO;

import java.util.List;

@Mapper
public interface BadgeMapper {

    @Select("SELECT id, user_id AS userId, badgepath AS badgePath, badgename AS badgeName FROM badge WHERE user_id = #{userId} ORDER BY id DESC")
    List<BadgeResponseDTO> listByUserId(@Param("userId") Long userId);

    @Select("SELECT COUNT(1) FROM badge WHERE user_id = #{userId} AND badgename = #{badgeName}")
    int countUserBadgeByName(@Param("userId") Long userId, @Param("badgeName") String badgeName);

    @Insert("INSERT INTO badge(user_id, badgepath, badgename) VALUES(#{userId}, #{badgePath}, #{badgeName})")
    int insertBadge(@Param("userId") Long userId, @Param("badgePath") String badgePath, @Param("badgeName") String badgeName);

    @Select("SELECT id FROM user_achievement WHERE user_id = #{userId} LIMIT 1")
    Long findAchievementId(@Param("userId") Long userId);

    @Select("SELECT checkin_days FROM user_achievement WHERE user_id = #{userId} LIMIT 1")
    Integer getCheckinDays(@Param("userId") Long userId);

    @Select("SELECT rightcount FROM user_achievement WHERE user_id = #{userId} LIMIT 1")
    Integer getRightCount(@Param("userId") Long userId);

    @Insert("INSERT INTO user_achievement(user_id, checkin_days, rightcount) VALUES(#{userId}, 0, 0)")
    int insertAchievement(@Param("userId") Long userId);

    @Update("UPDATE user_achievement SET checkin_days = #{days} WHERE user_id = #{userId}")
    int updateCheckinDays(@Param("userId") Long userId, @Param("days") Integer days);

    @Update("UPDATE user_achievement SET rightcount = #{count} WHERE user_id = #{userId}")
    int updateRightCount(@Param("userId") Long userId, @Param("count") Integer count);
}

