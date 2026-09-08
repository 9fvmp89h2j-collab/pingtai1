package org.example.springboot.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import jakarta.annotation.Resource;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import org.example.springboot.entity.User;
import org.example.springboot.entity.UserAcupointDailyProgress;
import org.example.springboot.entity.UserBackpack;
import org.example.springboot.entity.UserCollect;
import org.example.springboot.entity.UserCopperManProfile;
import org.example.springboot.entity.UserQuizMistake;
import org.example.springboot.entity.UserQuizStats;
import org.example.springboot.mapper.UserMapper;
import org.example.springboot.mapper.BadgeMapper;
import org.example.springboot.mapper.UserAcupointDailyProgressMapper;
import org.example.springboot.mapper.UserBackpackMapper;
import org.example.springboot.mapper.UserCollectMapper;
import org.example.springboot.mapper.UserCopperManProfileMapper;
import org.example.springboot.mapper.UserQuizMistakeMapper;
import org.example.springboot.mapper.UserQuizStatsMapper;
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
    LEVEL_1(1, 0, 10, "杏林见习侦探", "认识安全规则，准备开启中医文化探案"),
    LEVEL_2(2, 10, 20, "经络识图生", "能辨认身体地图中的经络文化路线"),
    LEVEL_3(3, 20, 30, "穴位观察员", "会观察穴位名称与身体区域，不做针刺操作"),
    LEVEL_4(4, 30, 40, "五行文化员", "理解五行文化中的分类与联系"),
    LEVEL_5(5, 40, 50, "铜人档案官", "能够整理小铜人的观察线索与文化档案"),
    LEVEL_6(6, 50, 60, "星点搜查员", "善于旋转观察并寻找隐藏的身体星点"),
    LEVEL_7(7, 60, 70, "经络小掌门", "能够复述经络路线的文化知识"),
    LEVEL_8(8, 70, 90, "安全守护使", "牢记只观察、只学习、不自己针刺"),
    LEVEL_9(9, 90, Integer.MAX_VALUE, "杏林金牌侦探", "完成九阶文化探案，获得成长纪念证书");

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

    private record AdventureMapLevel(
            String id,
            int order,
            String label,
            String unlockedMapKey,
            String route,
            int requiredLevel
    ) {}

    private static final List<AdventureMapLevel> ADVENTURE_MAP_LEVELS = List.of(
            new AdventureMapLevel("checkin", 1, "报到处", "checkin", "/checkin", 1),
            new AdventureMapLevel("safe-start", 2, "安全守护案", "safe-start", "/safety", 2),
            new AdventureMapLevel("bamboo", 3, "失踪竹简案", "bamboo", "/doctor-story", 3),
            new AdventureMapLevel("body", 4, "身体地图追踪案", "body", "/body-map", 4),
            new AdventureMapLevel("meridian", 5, "经络星河密令", "meridian", "/jingluo", 5),
            new AdventureMapLevel("archive", 6, "铜人档案室", "archive", "/copper-man", 6),
            new AdventureMapLevel("secret-room", 7, "星光修补册", "secret-room", "/review", 7),
            new AdventureMapLevel("agency", 8, "侦探社修复计划", "agency", "/agency", 8)
    );

    private static final String SHUNTING_GAME_CODE = "shunting";
    private static final int SHUNTING_DAILY_REWARD = 10;
    private static final int ONBOARDING_SCORE_REWARD = 10;

    @Resource
    private UserMapper userMapper;
    @Resource
    private BadgeService badgeService;
    @Resource
    private PasswordResetCodeService passwordResetCodeService;
    @Resource
    private BadgeMapper badgeMapper;
    @Resource
    private UserQuizStatsMapper userQuizStatsMapper;
    @Resource
    private UserQuizMistakeMapper userQuizMistakeMapper;
    @Resource
    private UserBackpackMapper userBackpackMapper;
    @Resource
    private UserCollectMapper userCollectMapper;
    @Resource
    private UserAcupointDailyProgressMapper userAcupointDailyProgressMapper;
    @Resource
    private UserCopperManProfileMapper userCopperManProfileMapper;
    @Resource
    private AuthSessionService authSessionService;
    @Resource
    private GameStateService gameStateService;

    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    /**
     * 用户登录
     * @param loginDTO 登录命令
     * @return 登录响应
     */
    @Transactional(rollbackFor = Exception.class)
    public UserLoginResponseDTO login(UserLoginCommandDTO loginDTO) {
        try {
            // 根据用户名或邮箱查找用户
            LambdaQueryWrapper<User> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(User::getUsername, loginDTO.getUsername())
                    .or()
                    .eq(User::getEmail, loginDTO.getUsername());
            User user = userMapper.selectOne(queryWrapper);


            if (user == null) {
                throw new BusinessException("用户名或密码错误");
            }

            // 验证密码
            if (!passwordEncoder.matches(loginDTO.getPassword(), user.getPassword())) {
                throw new BusinessException("用户名或密码错误");
            }

            // 检查用户状态
            if (!user.isActive()) {
                throw new BusinessException("用户名或密码错误，或账号不可用");
            }

            // 生成带可撤销会话ID的JWT token
            if (gameStateService != null) {
                gameStateService.initializeUser(user.getId());
            }

            String tokenId = UUID.randomUUID().toString();
            String token = JwtTokenUtils.generateToken(user.getId(), user.getUsername(), user.getUserType(), tokenId);
            if (authSessionService != null) {
                authSessionService.create(user.getId(), tokenId,
                        LocalDateTime.now().plusNanos(JwtTokenUtils.getExpirationMillis() * 1_000_000));
            }

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
            if (registerDTO.getUserType() != null
                    && !UserType.USER.getCode().equals(registerDTO.getUserType())) {
                throw new BusinessException("公开注册仅允许创建普通用户");
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
            user.setScore(ONBOARDING_SCORE_REWARD);

            userMapper.insert(user);
            if (gameStateService != null) {
                gameStateService.initializeUser(user.getId());
            }
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
    public Page<AdminUserListItemDTO> getUserPage(UserListQueryDTO queryDTO) {
        try {
            Page<User> page = new Page<>(queryDTO.getCurrentPage(), queryDTO.getSize());

            // 构建查询条件
            LambdaQueryWrapper<User> queryWrapper = new LambdaQueryWrapper<>();
            if (StringUtils.hasText(queryDTO.getKeyword())) {
                String keyword = queryDTO.getKeyword().trim();
                queryWrapper.and(wrapper -> wrapper
                        .like(User::getUsername, keyword)
                        .or()
                        .like(User::getName, keyword)
                        .or()
                        .like(User::getEmail, keyword));
            }
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
            Page<AdminUserListItemDTO> resultPage = new Page<>(userPage.getCurrent(), userPage.getSize(), userPage.getTotal());
            List<AdminUserListItemDTO> records = userPage.getRecords().stream()
                    .map(this::toAdminListItem)
                    .toList();
            resultPage.setRecords(records);

            return resultPage;

        } catch (Exception e) {
            log.error("查询用户列表失败", e);
            throw new ServiceException("查询失败，请稍后重试");
        }
    }

    public AdminUserSummaryResponseDTO getAdminUserSummary() {
        long totalUsers = userMapper.selectCount(null);
        long activeUsers = userMapper.selectCount(new LambdaQueryWrapper<User>().eq(User::getStatus, 1));
        long disabledUsers = userMapper.selectCount(new LambdaQueryWrapper<User>().eq(User::getStatus, 0));
        long adminUsers = userMapper.selectCount(new LambdaQueryWrapper<User>().eq(User::getUserType, UserType.ADMIN.getCode()));
        return AdminUserSummaryResponseDTO.builder()
                .totalUsers(totalUsers)
                .activeUsers(activeUsers)
                .disabledUsers(disabledUsers)
                .adminUsers(adminUsers)
                .build();
    }

    @Transactional(rollbackFor = Exception.class)
    public UserDetailResponseDTO adminCreateUser(Long operatorId, AdminUserCreateCommandDTO command) {
        if (!java.util.Objects.equals(command.getPassword(), command.getConfirmPassword())) {
            throw new BusinessException("两次输入的密码不一致");
        }
        ensureUniqueUsernameAndEmail(command.getUsername(), command.getEmail(), null);

        UserRegisterCommandDTO register = new UserRegisterCommandDTO();
        register.setUsername(command.getUsername());
        register.setEmail(command.getEmail());
        register.setPassword(command.getPassword());
        register.setConfirmPassword(command.getConfirmPassword());
        register.setName(command.getName());
        register.setAvatar(command.getAvatar());
        register.setPhone(command.getPhone());
        register.setSex(command.getSex());
        register.setUserType(UserType.USER.getCode());

        User user = UserConvert.registerCommandToEntity(register, passwordEncoder.encode(command.getPassword()));
        user.setAge(command.getAge());
        user.setScore(ONBOARDING_SCORE_REWARD);
        userMapper.insert(user);
        log.info("admin user created: operatorId={}, targetUserId={}, username={}", operatorId, user.getId(), user.getUsername());
        return UserConvert.entityToDetailResponse(user);
    }

    @Transactional(rollbackFor = Exception.class)
    public UserDetailResponseDTO adminUpdateProfile(Long operatorId, Long userId, AdminUserProfileUpdateCommandDTO command) {
        User user = requireUser(userId);
        if (command.getEmail() != null && !command.getEmail().equals(user.getEmail())) {
            ensureUniqueUsernameAndEmail(null, command.getEmail(), userId);
        }
        if (command.getEmail() != null) user.setEmail(command.getEmail());
        if (command.getName() != null) user.setName(command.getName());
        if (command.getAvatar() != null) user.setAvatar(command.getAvatar());
        if (command.getPhone() != null) user.setPhone(command.getPhone());
        if (command.getSex() != null) user.setSex(command.getSex());
        if (command.getAge() != null) user.setAge(command.getAge());
        user.setUpdatedAt(LocalDateTime.now());
        userMapper.updateById(user);
        log.info("admin user profile updated: operatorId={}, targetUserId={}", operatorId, userId);
        return UserConvert.entityToDetailResponse(user);
    }

    @Transactional(rollbackFor = Exception.class)
    public UserDetailResponseDTO adminUpdateStatus(Long operatorId, Long userId, Integer status) {
        if (status == null || (status != 0 && status != 1)) {
            throw new BusinessException("账号状态只能是0或1");
        }
        if (java.util.Objects.equals(operatorId, userId) && status == 0) {
            throw new BusinessException("不能禁用当前登录管理员账号");
        }
        User user = requireUser(userId);
        user.setStatus(status);
        user.setUpdatedAt(LocalDateTime.now());
        userMapper.updateById(user);
        log.info("admin user status changed: operatorId={}, targetUserId={}, status={}", operatorId, userId, status);
        return UserConvert.entityToDetailResponse(user);
    }

    @Transactional(rollbackFor = Exception.class)
    public void adminResetPassword(Long operatorId, Long userId, AdminUserPasswordResetCommandDTO command) {
        if (!java.util.Objects.equals(command.getNewPassword(), command.getConfirmPassword())) {
            throw new BusinessException("两次输入的密码不一致");
        }
        User user = requireUser(userId);
        user.setPassword(passwordEncoder.encode(command.getNewPassword()));
        user.setUpdatedAt(LocalDateTime.now());
        userMapper.updateById(user);
        log.info("admin user password reset: operatorId={}, targetUserId={}", operatorId, userId);
    }

    public AdminUserOverviewResponseDTO getAdminUserOverview(Long userId) {
        User user = requireUser(userId);
        UserLevelInfoResponseDTO level = buildLevelInfo(user.getScore());
        UserAdventureMapResponseDTO adventureMap = buildAdventureMapResponse(user, false);
        UserQuizStats quizStats = userQuizStatsMapper.selectOne(
                new LambdaQueryWrapper<UserQuizStats>().eq(UserQuizStats::getUserId, userId));
        int quizTotal = quizStats == null || quizStats.getTotalCount() == null ? 0 : quizStats.getTotalCount();
        int quizCorrect = quizStats == null || quizStats.getCorrectCount() == null ? 0 : quizStats.getCorrectCount();
        UserCopperManProfile copperMan = userCopperManProfileMapper.selectById(userId);
        List<BadgeResponseDTO> badges = badgeService.listUserBadges(userId);

        AdminUserOverviewResponseDTO.LearningStats learning = AdminUserOverviewResponseDTO.LearningStats.builder()
                .checkinDays(nvl(badgeMapper.getCheckinDays(userId)))
                .checkinCount(userMapper.countCheckinsByUserId(userId))
                .lastCheckinDate(userMapper.findLastCheckinDate(userId))
                .quizTotalCount(quizTotal)
                .quizCorrectCount(quizCorrect)
                .quizAccuracy(quizTotal == 0 ? 0 : (quizCorrect * 100.0 / quizTotal))
                .mistakeCount(userQuizMistakeMapper.selectCount(
                        new LambdaQueryWrapper<UserQuizMistake>().eq(UserQuizMistake::getUserId, userId)))
                .badgeCount(badges == null ? 0 : badges.size())
                .backpackCount(userBackpackMapper.selectCount(
                        new LambdaQueryWrapper<UserBackpack>().eq(UserBackpack::getUserId, userId)))
                .collectCount(userCollectMapper.selectCount(
                        new LambdaQueryWrapper<UserCollect>().eq(UserCollect::getUserId, userId)))
                .acupointDiscoveredCount(userAcupointDailyProgressMapper.selectCount(
                        new LambdaQueryWrapper<UserAcupointDailyProgress>().eq(UserAcupointDailyProgress::getUserId, userId)))
                .copperTokens(copperMan == null || copperMan.getCopperTokens() == null ? 0 : copperMan.getCopperTokens())
                .starSand(copperMan == null || copperMan.getStarSand() == null ? 0 : copperMan.getStarSand())
                .completedCases(copperMan == null || copperMan.getCompletedCases() == null ? 0 : copperMan.getCompletedCases())
                .lastCompletedCaseDate(copperMan == null ? null : copperMan.getLastCompletedCaseDate())
                .build();

        return AdminUserOverviewResponseDTO.builder()
                .user(toAdminListItem(user))
                .level(level)
                .adventureMap(adventureMap)
                .learning(learning)
                .badges(badges == null ? Collections.emptyList() : badges)
                .build();
    }

    private AdminUserListItemDTO toAdminListItem(User user) {
        QiBloodLevel level = QiBloodLevel.getLevelByScore(user.getScore() == null ? 0 : user.getScore());
        return AdminUserListItemDTO.builder()
                .id(user.getId())
                .username(user.getUsername())
                .displayName(user.getDisplayName())
                .name(user.getName())
                .avatar(user.getAvatar())
                .email(user.getEmail())
                .phone(user.getPhone())
                .sex(user.getSex())
                .age(user.getAge())
                .userType(user.getUserType())
                .userTypeDisplayName(user.getUserTypeDisplayName())
                .status(user.getStatus())
                .statusDisplayName(user.getStatusDisplayName())
                .score(user.getScore())
                .level(level.getLevel())
                .levelName(level.getLevelName())
                .honor(user.getHonor())
                .createdAt(user.getCreatedAt())
                .updatedAt(user.getUpdatedAt())
                .build();
    }

    private User requireUser(Long userId) {
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException("用户不存在");
        }
        return user;
    }

    private void ensureUniqueUsernameAndEmail(String username, String email, Long excludedUserId) {
        if (StringUtils.hasText(username)) {
            LambdaQueryWrapper<User> usernameQuery = new LambdaQueryWrapper<User>()
                    .eq(User::getUsername, username);
            if (excludedUserId != null) usernameQuery.ne(User::getId, excludedUserId);
            if (userMapper.selectCount(usernameQuery) > 0) throw new BusinessException("用户名已存在");
        }
        if (StringUtils.hasText(email)) {
            LambdaQueryWrapper<User> emailQuery = new LambdaQueryWrapper<User>()
                    .eq(User::getEmail, email);
            if (excludedUserId != null) emailQuery.ne(User::getId, excludedUserId);
            if (userMapper.selectCount(emailQuery) > 0) throw new BusinessException("邮箱已被其他用户使用");
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
    public UserDetailResponseDTO updateUser(Long userId, UserUpdateCommandDTO updateDTO, boolean allowPrivilegeChanges) {
        try {
            if (!allowPrivilegeChanges
                    && (updateDTO.getUserType() != null || updateDTO.getStatus() != null)) {
                throw new BusinessException("普通用户不能修改账号权限或状态");
            }
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
     * 通过邮箱一次性验证码重置密码
     * @param resetDTO 重置密码命令
     */
    @Transactional(rollbackFor = Exception.class)
    public void resetPasswordByVerification(UserResetPasswordCommandDTO resetDTO) {
        try {
            // 先校验一次性验证码，防止通过错误差异枚举用户名或邮箱。
            passwordResetCodeService.verifyAndConsume(
                    resetDTO.getUsername(), resetDTO.getEmail(), resetDTO.getVerificationCode());

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

            // 一次性验证码验证通过，重置密码
            user.setPassword(passwordEncoder.encode(resetDTO.getNewPassword()));
            user.setUpdatedAt(LocalDateTime.now());
            userMapper.updateById(user);

            log.info("用户密码重置成功（邮箱验证码验证）: {}", user.getUsername());

        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.error("密码重置失败", e);
            throw new ServiceException("密码重置失败，请稍后重试");
        }
    }

    public void sendPasswordResetCode(PasswordResetCodeRequestDTO requestDTO) {
        passwordResetCodeService.sendCode(requestDTO.getUsername(), requestDTO.getEmail());
    }

    /** 每日首次完成调车观察挑战时，由服务端发放固定奖励。 */
    @Transactional(rollbackFor = Exception.class)
    public UserDetailResponseDTO claimShuntingReward(Long userId) {
        try {
            User user = userMapper.selectById(userId);
            if (user == null) {
                throw new BusinessException("用户不存在");
            }

            boolean newlyGranted;
            if (gameStateService != null) {
                newlyGranted = gameStateService.claimShuntingReward(userId, LocalDate.now());
            } else {
                userMapper.insertGameReward(userId, "shunting", LocalDate.now(), SHUNTING_DAILY_REWARD);
                user.setScore((user.getScore() == null ? 0 : user.getScore()) + SHUNTING_DAILY_REWARD);
                user.setUpdatedAt(LocalDateTime.now());
                userMapper.updateById(user);
                newlyGranted = true;
            }
            if (!newlyGranted) {
                throw new BusinessException("今天的调车挑战奖励已经领取过了");
            }
            user = userMapper.selectById(userId);

            log.info("调车挑战每日奖励发放成功: userId={}, reward={}", userId, SHUNTING_DAILY_REWARD);
            return UserConvert.entityToDetailResponse(user);
        } catch (DuplicateKeyException e) {
            throw new BusinessException("今天的调车挑战奖励已经领取过了");
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.error("调车挑战奖励发放失败: userId={}", userId, e);
            throw new ServiceException("奖励发放失败，请稍后重试");
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

            return buildLevelInfo(user.getScore());

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
    public UserAdventureMapResponseDTO getUserAdventureMap(Long userId) {
        try {
            User user = userMapper.selectById(userId);
            if (user == null) {
                throw new BusinessException("用户不存在");
            }
            return buildAdventureMapResponse(user, true);
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.error("获取探险地图失败: userId={}", userId, e);
            throw new ServiceException("获取探险地图失败，请稍后重试");
        }
    }

    private UserLevelInfoResponseDTO buildLevelInfo(Integer score) {
        int currentScore = score == null ? 0 : score;
        QiBloodLevel level = QiBloodLevel.getLevelByScore(currentScore);
        double progress;
        if (level.getMaxScore() != Integer.MAX_VALUE) {
            progress = (double) (currentScore - level.getMinScore())
                    / (level.getMaxScore() - level.getMinScore()) * 100;
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
    }

    private UserAdventureMapResponseDTO buildAdventureMapResponse(User user, boolean allowBackfill) {
        if (allowBackfill && gameStateService != null) {
            gameStateService.synchronizeCompletedSafetyCaseProgress(user.getId());
            return buildServerAdventureMapResponse(user);
        }
        boolean needsOnboardingBackfill = user.getScore() == null || user.getScore() <= 0;
        int currentScore = needsOnboardingBackfill ? ONBOARDING_SCORE_REWARD : user.getScore();
        if (allowBackfill && needsOnboardingBackfill) {
            user.setScore(currentScore);
            user.setUpdatedAt(LocalDateTime.now());
            userMapper.updateById(user);
        }
        if (!allowBackfill && needsOnboardingBackfill) {
            currentScore = user.getScore() == null ? 0 : user.getScore();
        }
        int userLevel = QiBloodLevel.getLevelByScore(currentScore).getLevel();
        int currentOrder = ADVENTURE_MAP_LEVELS.stream()
                .filter(level -> userLevel >= level.requiredLevel())
                .mapToInt(AdventureMapLevel::order)
                .max()
                .orElse(1);

        String currentLevelId = null;
        String currentMapKey = null;
        List<UserAdventureMapLevelResponseDTO> levels = ADVENTURE_MAP_LEVELS.stream()
                .map(level -> {
                    boolean unlocked = userLevel >= level.requiredLevel();
                    boolean completed = level.order() < currentOrder;
                    boolean current = level.order() == currentOrder;
                    return UserAdventureMapLevelResponseDTO.builder()
                            .id(level.id())
                            .order(level.order())
                            .label(level.label())
                            .unlockedMapKey(level.unlockedMapKey())
                            .route(level.route())
                            .requiredLevel(level.requiredLevel())
                            .unlocked(unlocked)
                            .completed(completed)
                            .current(current)
                            .build();
                })
                .toList();

        for (UserAdventureMapLevelResponseDTO level : levels) {
            if (Boolean.TRUE.equals(level.getCurrent())) {
                currentLevelId = level.getId();
                currentMapKey = level.getUnlockedMapKey();
                break;
            }
        }

        return UserAdventureMapResponseDTO.builder()
                .currentScore(currentScore)
                .userLevel(userLevel)
                .currentLevelId(currentLevelId)
                .currentMapKey(currentMapKey)
                .levels(levels)
                .build();
    }

    private UserAdventureMapResponseDTO buildServerAdventureMapResponse(User user) {
        var serverState = gameStateService.getState(user.getId());
        int currentScore = serverState.getCurrentScore() == null ? 0 : serverState.getCurrentScore();
        int userLevel = serverState.getUserLevel() == null
                ? QiBloodLevel.getLevelByScore(currentScore).getLevel()
                : serverState.getUserLevel();
        var serverLevels = serverState.getLevels();

        List<UserAdventureMapLevelResponseDTO> levels = serverLevels.stream()
                .map(serverLevel -> UserAdventureMapLevelResponseDTO.builder()
                        .id(serverLevel.getId())
                        .order(serverLevel.getOrder())
                        .label(serverLevel.getLabel())
                        .unlockedMapKey(serverLevel.getId())
                        .route(serverLevel.getRoute())
                        .requiredLevel(serverLevel.getOrder())
                        .unlocked(serverLevel.getUnlocked())
                        .completed(serverLevel.getCompleted())
                        .current(serverLevel.getCurrent())
                        .status(serverLevel.getStatus())
                        .progress(serverLevel.getProgress())
                        .target(serverLevel.getTarget())
                        .requiredTaskIds(serverLevel.getRequiredTaskIds())
                        .nextLevelId(serverLevel.getNextLevelId())
                        .build())
                .toList();

        UserAdventureMapLevelResponseDTO current = levels.stream()
                .filter(level -> Boolean.TRUE.equals(level.getCurrent()))
                .findFirst()
                .orElse(levels.get(0));
        return UserAdventureMapResponseDTO.builder()
                .currentScore(currentScore)
                .userLevel(userLevel)
                .currentLevelId(current.getId())
                .currentMapKey(current.getUnlockedMapKey())
                .levels(levels)
                .build();
    }

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
            boolean newlyCheckedIn;
            if (gameStateService != null) {
                newlyCheckedIn = gameStateService.checkinToday(userId, today);
            } else {
                if (userMapper.countCheckinByDay(userId, today) > 0) {
                    newlyCheckedIn = false;
                } else {
                    userMapper.insertCheckin(userId, today);
                    user.setScore((user.getScore() == null ? 0 : user.getScore()) + 5);
                    user.setUpdatedAt(LocalDateTime.now());
                    userMapper.updateById(user);
                    newlyCheckedIn = true;
                }
            }
            if (!newlyCheckedIn) {
                throw new BusinessException("今天已经签到过了");
            }
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
            throw new ServiceException("签到失败，请稍后重试");
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

    /** 管理员只读查看指定用户的签到日历。 */
    public List<LocalDate> listCheckinDatesByMonthForAdmin(Long userId, Integer year, Integer month) {
        return listCheckinDatesByMonth(userId, year, month);
    }

    public List<BadgeResponseDTO> listUserBadges(Long userId) {
        if (userId == null) return Collections.emptyList();
        return badgeService.listUserBadges(userId);
    }

    private int nvl(Integer value) {
        return value == null ? 0 : value;
    }

}
