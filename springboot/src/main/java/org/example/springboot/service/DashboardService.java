package org.example.springboot.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.example.springboot.dto.response.DashboardStatisticsResponseDTO;
import org.example.springboot.entity.CommunityPost;
import org.example.springboot.entity.User;
import org.example.springboot.mapper.CommunityPostMapper;
import org.example.springboot.mapper.UserMapper;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.function.Supplier;

/**
 * 仪表板服务
 * @author system
 */
@Service
@Slf4j
public class DashboardService {

    @Resource
    private UserMapper userMapper;

    @Resource
    private CommunityPostMapper communityPostMapper;

    /**
     * 获取仪表板统计数据
     */
    public DashboardStatisticsResponseDTO getStatistics() {
        log.info("开始获取仪表板统计数据");

        Long totalUsers = safe("totalUsers", this::getTotalUsers, 0L);
        Long todayNewUsers = safe("todayNewUsers", this::getTodayNewUsers, 0L);
        Long totalAdmins = safe("totalAdmins", this::getTotalAdmins, 0L);
        Long totalPosts = safe("totalPosts", this::getTotalPosts, 0L);
        Long todayNewPosts = safe("todayNewPosts", this::getTodayNewPosts, 0L);
        Long totalVisits = safe("totalVisits", this::getTotalVisits, 0L);
        Long todayVisits = safe("todayVisits", this::getTodayVisits, 0L);
        List<DashboardStatisticsResponseDTO.DailyStatistics> last7DaysVisits =
                safe("last7DaysVisits", this::getLast7DaysVisits, buildZeroLast7Days());

        DashboardStatisticsResponseDTO statistics = DashboardStatisticsResponseDTO.builder()
                .totalUsers(totalUsers)
                .todayNewUsers(todayNewUsers)
                .totalAdmins(totalAdmins)
                .totalPosts(totalPosts)
                .todayNewPosts(todayNewPosts)
                .totalVisits(totalVisits)
                .todayVisits(todayVisits)
                // 向后兼容旧字段，避免其它页面仍读到 orders 时出错
                .totalOrders(totalPosts)
                .todayOrders(todayNewPosts)
                .last7DaysVisits(last7DaysVisits)
                .build();

        log.info("仪表板统计数据获取完成");
        return statistics;
    }

    private <T> T safe(String name, Supplier<T> supplier, T defaultValue) {
        try {
            return supplier.get();
        } catch (Exception e) {
            log.warn("仪表板统计 {} 获取失败，降级默认值: {}", name, e.getMessage());
            return defaultValue;
        }
    }

    /**
     * 获取总用户数
     */
    private Long getTotalUsers() {
        LambdaQueryWrapper<User> wrapper = Wrappers.lambdaQuery();
        wrapper.eq(User::getUserType, "USER");
        return userMapper.selectCount(wrapper);
    }

    /**
     * 获取今日新增用户数
     */
    private Long getTodayNewUsers() {
        LocalDateTime todayStart = LocalDate.now().atStartOfDay();
        LambdaQueryWrapper<User> wrapper = Wrappers.lambdaQuery();
        wrapper.eq(User::getUserType, "USER");
        wrapper.ge(User::getCreatedAt, todayStart);
        return userMapper.selectCount(wrapper);
    }

    /**
     * 获取总管理员数
     */
    private Long getTotalAdmins() {
        LambdaQueryWrapper<User> wrapper = Wrappers.lambdaQuery();
        wrapper.eq(User::getUserType, "ADMIN");
        return userMapper.selectCount(wrapper);
    }

    /**
     * 获取社区总发帖数（仅正常状态）
     */
    private Long getTotalPosts() {
        LambdaQueryWrapper<CommunityPost> wrapper = Wrappers.lambdaQuery();
        wrapper.eq(CommunityPost::getStatus, 0);
        return communityPostMapper.selectCount(wrapper);
    }

    /**
     * 获取今日新增发帖数（仅正常状态）
     */
    private Long getTodayNewPosts() {
        LocalDateTime todayStart = LocalDate.now().atStartOfDay();
        LambdaQueryWrapper<CommunityPost> wrapper = Wrappers.lambdaQuery();
        wrapper.eq(CommunityPost::getStatus, 0);
        wrapper.ge(CommunityPost::getCreateTime, todayStart);
        return communityPostMapper.selectCount(wrapper);
    }

    /**
     * 获取网站总访问数（直接查 site_visit）
     */
    private Long getTotalVisits() {
        return userMapper.selectCount(Wrappers.emptyWrapper()) >= 0
                ? executeCountSql("SELECT COUNT(*) FROM site_visit")
                : 0L;
    }

    /**
     * 获取今日访问数
     */
    private Long getTodayVisits() {
        LocalDate today = LocalDate.now();
        String start = today.atStartOfDay().toString().replace('T', ' ');
        String end = today.atTime(LocalTime.MAX).toString().replace('T', ' ');
        return executeCountSql("SELECT COUNT(*) FROM site_visit WHERE visit_time BETWEEN '" + start + "' AND '" + end + "'");
    }

    /**
     * 获取近7天访问趋势
     */
    private List<DashboardStatisticsResponseDTO.DailyStatistics> getLast7DaysVisits() {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MM-dd");
        List<DashboardStatisticsResponseDTO.DailyStatistics> result = new ArrayList<>();

        for (int i = 6; i >= 0; i--) {
            LocalDate day = LocalDate.now().minusDays(i);
            String start = day.atStartOfDay().toString().replace('T', ' ');
            String end = day.atTime(LocalTime.MAX).toString().replace('T', ' ');
            Long count = executeCountSql("SELECT COUNT(*) FROM site_visit WHERE visit_time BETWEEN '" + start + "' AND '" + end + "'");

            result.add(DashboardStatisticsResponseDTO.DailyStatistics.builder()
                    .date(day.format(formatter))
                    .count(count)
                    .build());
        }
        return result;
    }

    /**
     * 近7天空数据兜底
     */
    private List<DashboardStatisticsResponseDTO.DailyStatistics> buildZeroLast7Days() {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MM-dd");
        List<DashboardStatisticsResponseDTO.DailyStatistics> result = new ArrayList<>();
        for (int i = 6; i >= 0; i--) {
            LocalDate date = LocalDate.now().minusDays(i);
            result.add(DashboardStatisticsResponseDTO.DailyStatistics.builder()
                    .date(date.format(formatter))
                    .count(0L)
                    .build());
        }
        return result;
    }

    /**
     * 执行 count SQL（避免依赖新增 mapper，防止你误改后又缺文件）
     */
    private Long executeCountSql(String sql) {
        return userMapper.selectCountByRawSql(sql);
    }

}

