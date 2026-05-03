package org.example.springboot.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import jakarta.annotation.Resource;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.Collections;
import java.util.List;

import org.example.springboot.entity.User;
import org.example.springboot.mapper.UserMapper;
import org.example.springboot.dto.command.*;
import org.example.springboot.dto.query.*;
import org.example.springboot.dto.response.*;
import org.example.springboot.enums.UserType;
import org.example.springboot.exception.BusinessException;
import org.example.springboot.exception.ServiceException;
import org.example.springboot.util.JwtTokenUtils;
import org.example.springboot.util.FileUtil;
import org.example.springboot.service.convert.UserConvert;
import org.springframework.web.multipart.MultipartFile;

/**
 * 气血能量等级配置
 */
enum QiBloodLevel {
    LEVEL_1(1, 0, 10, "萌芽小郎中", "刚刚接触针灸，开始感知身体的能量"),
    LEVEL_2(2, 10, 20, "经络识图生", "能认出身体里的 12 条能量大动脉"),
    LEVEL_3(3, 20, 30, "穴位探险家", "掌握了基础穴位的召唤方法，能处理简单痛症"),
    LEVEL_4(4, 30, 40, "五行调理师", "理解了人体平衡的艺术，能进行组合配穴"),
    LEVEL_5(5, 40, 50, "妙手大医官", "气血充盈，通达经络，守护身体安康"),
    LEVEL_6(6, 50, 60, "银针小宗师", "融会贯通，以针调气愈疾"),
    LEVEL_7(7, 60, 70, "经络小掌门", "统领诸经，辨证施术"),
    LEVEL_8(8, 70, 80, "小银针大魔法使", "已臻化境，小银针见大魔法"),
    LEVEL_9(9, 90, Integer.MAX_VALUE, "针道传承荣耀使", "修满九阶，气血圆融，特颁此证以纪成长");

    private final int level;
    private final int minScore;
    private final int maxScore;
    private final String levelName;
    private final String description;

    QiBloodLevel(int level, int minScore, int maxScore, String levelName, String description) {
        this.level = level;
        this.minScore = minScore;
        this.maxScore = maxScore;
        this.levelName = levelName;
        this.description = description;
    }

    public int getLevel() {
        return level;
    }

    public int getMinScore() {
        return minScore;
    }

    public int getMaxScore() {
        return maxScore;
    }

    public String getLevelName() {
        return levelName;
    }

    public String getDescription() {
        return description;
    }

    public static QiBloodLevel getLevelByScore(int score) {
        for (QiBloodLevel level : values()) {
            if (score >= level.minScore && score < level.maxScore) {
                return level;
            }
        }
        return LEVEL_9;
    }
}

/**
 * 用户业务逻辑层
 * @author ftfx
 */
@Slf4j
@Service
public class UserService {

    @Resource
    private UserMapper userMapper;
    @Resource
    private BadgeService badgeService;

    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    /**
     * 用户登录
     * @param loginDTO 登录命令
     * @return 登录响应
     */
    public UserLoginResponseDTO login(UserLoginCommandDTO loginDTO) {
        try {
            // 根据用户名或邮箱查找用户
            LambdaQueryWrapper<User> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(User::getUsername, loginDTO.getUsername())
                    .or()
                    .eq(User::getEmail, loginDTO.getUsername());
            User user = userMapper.selectOne(queryWrapper);


            if (user == null) {
                throw new BusinessException("用户不存在");
            }

            // 验证密码
            if (!passwordEncoder.matches(loginDTO.getPassword(), user.getPassword())) {
                throw new BusinessException("用户名或密码错误");
            }

            // 检查用户状态
            if (!user.isActive()) {
                throw new BusinessException("账号已被禁用，请联系管理员");
            }

            // 生成JWT token
            String token = JwtTokenUtils.generateToken(user.getId(), user.getUsername(), user.getUserType());

            // 构建响应
            UserDetailResponseDTO userInfo = UserConvert.entityToDetailResponse(user);
            return UserConvert.buildLoginResponse(token, userInfo);

        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.error("用户登录失败", e);
            throw new ServiceException("登录失败，请稍后重试");
        }
    }

    /**
     * 用户注册
     * @param registerDTO 注册命令
     * @return 用户信息
     */
    @Transactional(rollbackFor = Exception.class)
    public UserDetailResponseDTO register(UserRegisterCommandDTO registerDTO) {
        try {
            // 验证密码确认
            if (!registerDTO.getPassword().equals(registerDTO.getConfirmPassword())) {
                throw new BusinessException("两次输入的密码不一致");
            }

            // 检查用户名是否存在
            LambdaQueryWrapper<User> usernameQuery = new LambdaQueryWrapper<>();
            usernameQuery.eq(User::getUsername, registerDTO.getUsername());
            if (userMapper.selectCount(usernameQuery) > 0) {
                throw new BusinessException("用户名已存在");
            }

            // 检查邮箱是否存在
            LambdaQueryWrapper<User> emailQuery = new LambdaQueryWrapper<>();
            emailQuery.eq(User::getEmail, registerDTO.getEmail());
            if (userMapper.selectCount(emailQuery) > 0) {
                throw new BusinessException("邮箱已被注册");
            }

            // 验证用户类型
            if (!UserType.isValidCode(registerDTO.getUserType())) {
                throw new BusinessException("无效的用户类型");
            }

            // 创建用户
            String encodedPassword = passwordEncoder.encode(registerDTO.getPassword());
            User user = UserConvert.registerCommandToEntity(registerDTO, encodedPassword);

            userMapper.insert(user);
            log.info("用户注册成功: {}", user.getUsername());

            return UserConvert.entityToDetailResponse(user);

        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.error("用户注册失败", e);
            throw new ServiceException("注册失败，请稍后重试");
        }
    }

    /**
     * 根据ID获取用户信息
     * @param userId 用户ID
     * @return 用户信息
     */
    public UserDetailResponseDTO getUserById(Long userId) {
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException("用户不存在");
        }
        return UserConvert.entityToDetailResponse(user);
    }



    /**
     * 分页查询用户列表
     * @param queryDTO 查询条件
     * @return 用户分页列表
     */
    public Page<UserDetailResponseDTO> getUserPage(UserListQueryDTO queryDTO) {
        try {
            Page<User> page = new Page<>(queryDTO.getCurrentPage(), queryDTO.getSize());

            // 构建查询条件
            LambdaQueryWrapper<User> queryWrapper = new LambdaQueryWrapper<>();
            if (StringUtils.hasText(queryDTO.getUsername())) {
                queryWrapper.like(User::getUsername, queryDTO.getUsername());
            }
            if (StringUtils.hasText(queryDTO.getEmail())) {
                queryWrapper.like(User::getEmail, queryDTO.getEmail());
            }

            if (StringUtils.hasText(queryDTO.getName())) {
                queryWrapper.like(User::getName, queryDTO.getName());
            }
            if (StringUtils.hasText(queryDTO.getUserType())) {
                queryWrapper.eq(User::getUserType, queryDTO.getUserType());
            }
            if (queryDTO.getStatus() != null) {
                queryWrapper.eq(User::getStatus, queryDTO.getStatus());
            }
            queryWrapper.orderByDesc(User::getCreatedAt);

            Page<User> userPage = userMapper.selectPage(page, queryWrapper);

            // 转换为响应DTO
            Page<UserDetailResponseDTO> resultPage = new Page<>(userPage.getCurrent(), userPage.getSize(), userPage.getTotal());
            List<UserDetailResponseDTO> records = userPage.getRecords().stream()
                    .map(UserConvert::entityToDetailResponse)
                    .toList();
            resultPage.setRecords(records);

            return resultPage;

        } catch (Exception e) {
            log.error("查询用户列表失败", e);
            throw new ServiceException("查询失败，请稍后重试");
        }
    }

    /**
     * 删除用户
     * @param userId 用户ID
     */
    @Transactional(rollbackFor = Exception.class)
    public void deleteUser(Long userId) {
        try {
            User user = userMapper.selectById(userId);
            if (user == null) {
                throw new BusinessException("用户不存在");
            }

            // 检查是否为管理员（防止删除管理员）
            if (user.isAdmin()) {
                throw new BusinessException("不能删除管理员账号");
            }

            userMapper.deleteById(userId);
            log.info("用户删除成功: {}", user.getUsername());

        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.error("用户删除失败", e);
            throw new ServiceException("删除失败，请稍后重试");
        }
    }





    /**
     * 更新用户信息
     * @param userId 用户ID
     * @param updateDTO 更新信息
     * @return 更新后的用户信息
     */
    @Transactional(rollbackFor = Exception.class)
    public UserDetailResponseDTO updateUser(Long userId, UserUpdateCommandDTO updateDTO) {
        try {
            User user = userMapper.selectById(userId);
            if (user == null) {
                throw new BusinessException("用户不存在");
            }

            // 检查邮箱是否已被其他用户使用
            if (updateDTO.getEmail() != null && !updateDTO.getEmail().equals(user.getEmail())) {
                LambdaQueryWrapper<User> emailQuery = new LambdaQueryWrapper<>();
                emailQuery.eq(User::getEmail, updateDTO.getEmail())
                        .ne(User::getId, userId);
                if (userMapper.selectCount(emailQuery) > 0) {
                    throw new BusinessException("邮箱已被其他用户使用");
                }
            }

            // 验证用户类型和状态的有效性
            if (updateDTO.getUserType() != null && !UserType.isValidCode(updateDTO.getUserType())) {
                throw new BusinessException("无效的用户类型");
            }

            // 应用更新
            UserConvert.applyUpdateToEntity(user, updateDTO);
            userMapper.updateById(user);

            log.info("用户信息更新成功: {}", user.getUsername());
            return UserConvert.entityToDetailResponse(user);

        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.error("用户信息更新失败", e);
            throw new ServiceException("更新失败，请稍后重试");
        }
    }

    /**
     * 修改用户密码
     * @param userId 用户ID
     * @param passwordDTO 密码更新信息
     */
    @Transactional(rollbackFor = Exception.class)
    public void updatePassword(Long userId, UserPasswordUpdateCommandDTO passwordDTO) {
        try {
            User user = userMapper.selectById(userId);
            if (user == null) {
                throw new BusinessException("用户不存在");
            }

            // 验证旧密码
            if (!passwordEncoder.matches(passwordDTO.getOldPassword(), user.getPassword())) {
                throw new BusinessException("旧密码不正确");
            }

            // 更新密码
            user.setPassword(passwordEncoder.encode(passwordDTO.getNewPassword()));
            user.setUpdatedAt(LocalDateTime.now());
            userMapper.updateById(user);

            log.info("用户密码修改成功: {}", user.getUsername());

        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.error("密码修改失败", e);
            throw new ServiceException("密码修改失败，请稍后重试");
        }
    }

    /**
     * 通过邮箱重置密码
     * @param email 邮箱
     * @param newPassword 新密码
     */
    @Transactional(rollbackFor = Exception.class)
    public void resetPasswordByEmail(String email, String newPassword) {
        try {
            LambdaQueryWrapper<User> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(User::getEmail, email);
            User user = userMapper.selectOne(queryWrapper);

            if (user == null) {
                throw new BusinessException("邮箱不存在");
            }

            // 重置密码
            user.setPassword(passwordEncoder.encode(newPassword));
            user.setUpdatedAt(LocalDateTime.now());
            userMapper.updateById(user);

            log.info("用户密码重置成功: {}", user.getUsername());

        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.error("密码重置失败", e);
            throw new ServiceException("密码重置失败，请稍后重试");
        }
    }

    /**
     * 通过用户名、邮箱、手机号三要素验证后重置密码
     * @param resetDTO 重置密码命令
     */
    @Transactional(rollbackFor = Exception.class)
    public void resetPasswordByVerification(UserResetPasswordCommandDTO resetDTO) {
        try {
            // 根据用户名查询用户
            LambdaQueryWrapper<User> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(User::getUsername, resetDTO.getUsername());
            User user = userMapper.selectOne(queryWrapper);

            if (user == null) {
                throw new BusinessException("用户名不存在");
            }

            // 验证邮箱是否匹配
            if (!resetDTO.getEmail().equals(user.getEmail())) {
                throw new BusinessException("邮箱与用户名不匹配");
            }

            // 验证手机号是否匹配
            if (!resetDTO.getPhone().equals(user.getPhone())) {
                throw new BusinessException("手机号与用户名不匹配");
            }

            // 三要素验证通过，重置密码
            user.setPassword(passwordEncoder.encode(resetDTO.getNewPassword()));
            user.setUpdatedAt(LocalDateTime.now());
            userMapper.updateById(user);

            log.info("用户密码重置成功（三要素验证）: {}", user.getUsername());

        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.error("密码重置失败", e);
            throw new ServiceException("密码重置失败，请稍后重试");
        }
    }

    /**
     * 更新用户气血能量
     * @param userId 用户ID
     * @param scoreDTO 气血能量更新DTO
     * @return 更新后的用户信息
     */
    @Transactional(rollbackFor = Exception.class)
    public UserDetailResponseDTO updateUserScore(Long userId, UserScoreUpdateCommandDTO scoreDTO) {
        try {
            User user = userMapper.selectById(userId);
            if (user == null) {
                throw new BusinessException("用户不存在");
            }

            // 初始化气血能量为0
            if (user.getScore() == null) {
                user.setScore(0);
            }

            // 更新气血能量
            int newScore = user.getScore() + scoreDTO.getScoreChange();
            if (newScore < 0) {
                throw new BusinessException("气血能量不能为负数");
            }
            user.setScore(newScore);
            user.setUpdatedAt(LocalDateTime.now());
            userMapper.updateById(user);

            log.info("用户气血能量更新成功: userId={}, change={}, reason={}", userId, scoreDTO.getScoreChange(), scoreDTO.getReason());
            return UserConvert.entityToDetailResponse(user);

        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.error("更新气血能量失败: userId={}", userId, e);
            throw new ServiceException("更新气血能量失败，请稍后重试");
        }
    }

    /**
     * 获取用户气血能量等级信息
     * @param userId 用户ID
     * @return 等级信息
     */
    public UserLevelInfoResponseDTO getUserLevelInfo(Long userId) {
        try {
            User user = userMapper.selectById(userId);
            if (user == null) {
                throw new BusinessException("用户不存在");
            }

            // 初始化气血能量为0
            int currentScore = user.getScore() != null ? user.getScore() : 0;

            // 获取当前等级
            QiBloodLevel level = QiBloodLevel.getLevelByScore(currentScore);

            // 计算进度条百分比
            double progress = 0.0;
            if (level.getMaxScore() != Integer.MAX_VALUE) {
                progress = (double) (currentScore - level.getMinScore()) / (level.getMaxScore() - level.getMinScore()) * 100;
            } else {
                progress = 100.0;
            }

            return UserLevelInfoResponseDTO.builder()
                    .currentScore(currentScore)
                    .level(level.getLevel())
                    .levelName(level.getLevelName())
                    .description(level.getDescription())
                    .minScore(level.getMinScore())
                    .maxScore(level.getMaxScore() == Integer.MAX_VALUE ? null : level.getMaxScore())
                    .progress(progress)
                    .build();

        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.error("获取用户等级信息失败: userId={}", userId, e);
            throw new ServiceException("获取等级信息失败，请稍后重试");
        }
    }

    /**
     * 上传并保存九级荣誉证书（仅气血等级已达 9 级且尚未颁发时可写入文件；已存在则直接返回）
     */
    @Transactional(rollbackFor = Exception.class)
    public UserDetailResponseDTO uploadLevelCertificate(Long userId, MultipartFile file) {
        try {
            User user = userMapper.selectById(userId);
            if (user == null) {
                throw new BusinessException("用户不存在");
            }
            int currentScore = user.getScore() != null ? user.getScore() : 0;
            QiBloodLevel level = QiBloodLevel.getLevelByScore(currentScore);
            if (level.getLevel() < 9) {
                throw new BusinessException("仅九级用户可领取荣誉证书");
            }
            if (StringUtils.hasText(user.getHonor())) {
                return UserConvert.entityToDetailResponse(user);
            }
            String relativePath = FileUtil.saveFile(file, "bussiness", "user_avatar");
            user.setHonor(relativePath);
            user.setUpdatedAt(LocalDateTime.now());
            userMapper.updateById(user);
            log.info("用户荣誉证书已保存: userId={}, path={}", userId, relativePath);
            return UserConvert.entityToDetailResponse(userMapper.selectById(userId));
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.error("保存荣誉证书失败: userId={}", userId, e);
            throw new ServiceException("保存荣誉证书失败，请稍后重试");
        }
    }

    @Transactional(rollbackFor = Exception.class)
    public UserCheckinResultDTO checkinToday(Long userId) {
        try {
            User user = userMapper.selectById(userId);
            if (user == null) {
                throw new BusinessException("用户不存在");
            }
            LocalDate today = LocalDate.now();
            if (userMapper.countCheckinByDay(userId, today) > 0) {
                throw new BusinessException("今天已经签到过了");
            }
            userMapper.insertCheckin(userId, today);
            int base = user.getScore() == null ? 0 : user.getScore();
            user.setScore(base + 5);
            user.setUpdatedAt(LocalDateTime.now());
            userMapper.updateById(user);
            UserCheckinResultDTO out = new UserCheckinResultDTO();
            out.setUserInfo(UserConvert.entityToDetailResponse(userMapper.selectById(userId)));
            try {
                out.setEarnedBadgeNames(badgeService.onCheckin(userId));
            } catch (Exception ex) {
                // 徽章系统异常不影响签到主流程（避免因表结构不一致导致无法签到）
                log.warn("签到成功但发放徽章失败: userId={}, err={}", userId, ex.getMessage(), ex);
                out.setEarnedBadgeNames(Collections.emptyList());
            }
            return out;
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.error("签到失败: userId={}", userId, e);
            throw new ServiceException("签到失败：" + (e.getMessage() != null ? e.getMessage() : "请稍后重试"));
        }
    }

    public List<LocalDate> listCheckinDatesByMonth(Long userId, Integer year, Integer month) {
        try {
            User user = userMapper.selectById(userId);
            if (user == null) {
                throw new BusinessException("用户不存在");
            }
            int y = year != null ? year : LocalDate.now().getYear();
            int m = month != null ? month : LocalDate.now().getMonthValue();
            YearMonth ym = YearMonth.of(y, m);
            return userMapper.listCheckinDates(userId, ym.atDay(1), ym.atEndOfMonth());
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.error("获取签到日历失败: userId={}", userId, e);
            throw new ServiceException("获取签到日历失败，请稍后重试");
        }
    }

    public List<String> awardIllnessBadge(Long userId, Integer illnessId) {
        if (userId == null) return Collections.emptyList();
        return badgeService.onIllnessSolved(userId, illnessId);
    }

    public List<BadgeResponseDTO> listUserBadges(Long userId) {
        if (userId == null) return Collections.emptyList();
        return badgeService.listUserBadges(userId);
    }

}
