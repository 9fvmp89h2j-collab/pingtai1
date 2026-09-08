package org.example.springboot.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.example.springboot.dto.command.*;
import org.example.springboot.dto.query.UserListQueryDTO;
import org.example.springboot.dto.response.UserAdventureMapResponseDTO;
import org.example.springboot.dto.response.UserDetailResponseDTO;
import org.example.springboot.dto.response.UserCheckinResultDTO;
import org.example.springboot.dto.response.UserLevelInfoResponseDTO;
import org.example.springboot.dto.response.UserLoginResponseDTO;
import org.example.springboot.dto.response.BadgeResponseDTO;
import org.example.springboot.dto.response.AdminUserListItemDTO;
import org.example.springboot.dto.response.AdminUserOverviewResponseDTO;
import org.example.springboot.dto.response.AdminUserSummaryResponseDTO;
import org.example.springboot.common.Result;
import org.example.springboot.exception.BusinessException;
import org.example.springboot.exception.ServiceException;
import org.example.springboot.enums.UserType;
import org.example.springboot.service.UserService;
import org.example.springboot.service.QuizService;
import org.example.springboot.service.LoginAttemptService;
import org.example.springboot.service.AuthSessionService;
import org.example.springboot.util.JwtTokenUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;
import java.util.List;

/**
 * 用户管理控制器
 * @author system
 */
@Tag(name = "用户管理")
@RestController
@Slf4j
@RequestMapping("/user")
public class UserController {

    @Resource
    private UserService userService;
    @Resource
    private LoginAttemptService loginAttemptService;
    @Resource
    private QuizService quizService;
    @Resource
    private AuthSessionService authSessionService;

    /**
     * 用户登录
     */
    @Operation(summary = "用户登录")
    @PostMapping("/login")
    public Result<UserLoginResponseDTO> login(
            @Valid @RequestBody UserLoginCommandDTO loginDTO,
            HttpServletRequest request) {
        String remoteAddress = getClientAddress(request);
        try {
            loginAttemptService.checkAllowed(remoteAddress, loginDTO.getUsername());
            log.info("用户登录请求: {}", loginDTO.getUsername());
            UserLoginResponseDTO response = userService.login(loginDTO);
            loginAttemptService.clear(remoteAddress, loginDTO.getUsername());
            return Result.success("登录成功", response);
        } catch (Exception e) {
            loginAttemptService.recordFailure(remoteAddress, loginDTO.getUsername());
            log.warn("用户登录失败: username={}, remoteAddress={}", loginDTO.getUsername(), remoteAddress);
            return Result.error(safeErrorMessage(e, "登录失败，请稍后重试"));
        }
    }

    /**
     * 用户注册
     */
    @Operation(summary = "用户注册")
    @PostMapping("/add")
    public Result<UserDetailResponseDTO> register(@Valid @RequestBody UserRegisterCommandDTO registerDTO) {
        log.info("用户注册请求: {}", registerDTO.getUsername());
        UserDetailResponseDTO response = userService.register(registerDTO);
        return Result.success("注册成功", response);
    }

    @Operation(summary = "退出登录并撤销当前会话")
    @PostMapping("/logout")
    public Result<Void> logout() {
        Long userId = JwtTokenUtils.getCurrentUserId();
        authSessionService.revoke(userId, JwtTokenUtils.getCurrentTokenId(), "user_request");
        return Result.success();
    }




    @Operation(summary = "发送密码重置验证码")
    @PostMapping("/forget/code")
    public Result<Void> sendPasswordResetCode(@Valid @RequestBody PasswordResetCodeRequestDTO requestDTO) {
        userService.sendPasswordResetCode(requestDTO);
        // 无论账号是否存在均返回相同结果，避免泄露注册信息。
        return Result.success();
    }

    /**
     * 忘记密码 - 通过邮箱一次性验证码重置密码
     */
    @Operation(summary = "忘记密码")
    @PostMapping("/forget")
    public Result<Void> forgetPassword(@Valid @RequestBody UserResetPasswordCommandDTO resetDTO) {
        log.info("忘记密码请求: username={}", resetDTO.getUsername());
        userService.resetPasswordByVerification(resetDTO);
        return Result.success();
    }




    /**
     * 获取当前登录用户信息
     */
    @Operation(summary = "获取当前登录用户信息")
    @GetMapping("/current")
    public Result<UserDetailResponseDTO> getCurrentUser() {
        try {
            UserDetailResponseDTO currentUser = JwtTokenUtils.getCurrentUser();
            if (currentUser == null) {
                return Result.error("未登录或登录已过期");
            }
            UserDetailResponseDTO fresh = userService.getUserById(currentUser.getId());
            log.info("获取当前用户信息: {}", fresh.getUsername());
            return Result.success(fresh);
        } catch (Exception e) {
            log.error("获取当前用户信息失败", e);
            return Result.error("获取用户信息失败");
        }
    }

    /**
     * 更新用户信息
     */
    @Operation(summary = "更新用户信息")
    @PutMapping("/{id}")
    public Result<UserDetailResponseDTO> updateUser(
            @Parameter(description = "用户ID") @PathVariable Long id,
            @Valid @RequestBody UserUpdateCommandDTO updateDTO) {
        
        try {
            UserDetailResponseDTO currentUser = JwtTokenUtils.getCurrentUser();
            if (currentUser == null) {
                return Result.error("未登录或登录已过期");
            }
            
            // 只能更新自己的信息，除非是管理员
            boolean isAdmin = UserType.ADMIN.getCode().equals(currentUser.getUserType());
            if (!currentUser.getId().equals(id) && !isAdmin) {
                return Result.error("无权限修改其他用户信息");
            }
            
            log.info("更新用户信息: userId={}", id);
            UserDetailResponseDTO response = userService.updateUser(id, updateDTO, isAdmin);
            return Result.success("更新成功", response);
        } catch (Exception e) {
            log.error("更新用户信息失败: userId={}", id, e);
            return Result.error(safeErrorMessage(e, "更新失败，请稍后重试"));
        }
    }

    /**
     * 修改用户密码
     */
    @Operation(summary = "修改用户密码")
    @PutMapping("/password/{id}")
    public Result<Void> updatePassword(
            @Parameter(description = "用户ID") @PathVariable Long id,
            @Valid @RequestBody UserPasswordUpdateCommandDTO passwordDTO) {
        
        try {
            UserDetailResponseDTO currentUser = JwtTokenUtils.getCurrentUser();
            if (currentUser == null) {
                return Result.error("未登录或登录已过期");
            }
            
            // 只能修改自己的密码
            if (!currentUser.getId().equals(id)) {
                return Result.error("无权限修改其他用户密码");
            }
            
            log.info("修改用户密码: userId={}", id);
            userService.updatePassword(id, passwordDTO);
            return Result.success();
        } catch (Exception e) {
            log.error("修改用户密码失败: userId={}", id, e);
            return Result.error(safeErrorMessage(e, "密码修改失败，请稍后重试"));
        }
    }

    /**
     * 分页查询用户列表（管理员功能）
     */
    @Operation(summary = "分页查询用户列表")
    @GetMapping("/page")
    public Result<Page<AdminUserListItemDTO>> getUserPage(
            @Parameter(description = "用户名、姓名或邮箱关键词") @RequestParam(required = false) String keyword,
            @Parameter(description = "用户名") @RequestParam(required = false) String username,
            @Parameter(description = "邮箱") @RequestParam(required = false) String email,
            @Parameter(description = "姓名") @RequestParam(required = false) String name,
            @Parameter(description = "用户类型") @RequestParam(required = false) String userType,
            @Parameter(description = "用户状态") @RequestParam(required = false) Integer status,
            @Parameter(description = "当前页码") @RequestParam(defaultValue = "1") Integer currentPage,
            @Parameter(description = "每页大小") @RequestParam(defaultValue = "10") Integer size
          ) {

        // 权限检查：只有管理员可以查看用户列表
        UserDetailResponseDTO currentUser = JwtTokenUtils.getCurrentUser();
        if (currentUser == null || !UserType.ADMIN.getCode().equals(currentUser.getUserType())) {
            return Result.error("权限不足");
        }

        UserListQueryDTO queryDTO = new UserListQueryDTO();
        queryDTO.setKeyword(keyword);
        queryDTO.setUsername(username);
        queryDTO.setEmail(email);
        queryDTO.setName(name);
        queryDTO.setUserType(userType);
        queryDTO.setStatus(status);
        queryDTO.setCurrentPage(currentPage);
        queryDTO.setSize(size);

        log.info("管理员查询用户列表: page={}, size={}", currentPage, size);
        Page<AdminUserListItemDTO> response = userService.getUserPage(queryDTO);
        return Result.success(response);
    }

    @Operation(summary = "获取用户管理页统计")
    @GetMapping("/admin/summary")
    public Result<AdminUserSummaryResponseDTO> getAdminUserSummary() {
        try {
            UserDetailResponseDTO operator = requireAdmin();
            log.info("管理员查看用户统计: operatorId={}", operator.getId());
            return Result.success(userService.getAdminUserSummary());
        } catch (Exception e) {
            return Result.error(safeErrorMessage(e, "获取用户统计失败，请稍后重试"));
        }
    }

    @Operation(summary = "管理员创建普通用户")
    @PostMapping("/admin/create")
    public Result<UserDetailResponseDTO> adminCreateUser(
            @Valid @RequestBody AdminUserCreateCommandDTO command) {
        try {
            UserDetailResponseDTO operator = requireAdmin();
            return Result.success("普通用户创建成功", userService.adminCreateUser(operator.getId(), command));
        } catch (Exception e) {
            return Result.error(safeErrorMessage(e, "创建用户失败，请稍后重试"));
        }
    }

    @Operation(summary = "管理员获取用户学习档案")
    @GetMapping("/admin/overview/{id}")
    public Result<AdminUserOverviewResponseDTO> getAdminUserOverview(
            @Parameter(description = "用户ID") @PathVariable Long id) {
        try {
            UserDetailResponseDTO operator = requireAdmin();
            log.info("管理员查看用户档案: operatorId={}, targetUserId={}", operator.getId(), id);
            return Result.success(userService.getAdminUserOverview(id));
        } catch (Exception e) {
            return Result.error(safeErrorMessage(e, "获取用户档案失败，请稍后重试"));
        }
    }

    @Operation(summary = "管理员编辑用户资料")
    @PutMapping("/admin/{id}/profile")
    public Result<UserDetailResponseDTO> adminUpdateUserProfile(
            @Parameter(description = "用户ID") @PathVariable Long id,
            @Valid @RequestBody AdminUserProfileUpdateCommandDTO command) {
        try {
            UserDetailResponseDTO operator = requireAdmin();
            return Result.success("用户资料更新成功", userService.adminUpdateProfile(operator.getId(), id, command));
        } catch (Exception e) {
            return Result.error(safeErrorMessage(e, "更新用户资料失败，请稍后重试"));
        }
    }

    @Operation(summary = "管理员切换用户状态")
    @PutMapping("/admin/{id}/status")
    public Result<UserDetailResponseDTO> adminUpdateUserStatus(
            @Parameter(description = "用户ID") @PathVariable Long id,
            @Valid @RequestBody AdminUserStatusUpdateCommandDTO command) {
        try {
            UserDetailResponseDTO operator = requireAdmin();
            return Result.success("账号状态更新成功", userService.adminUpdateStatus(operator.getId(), id, command.getStatus()));
        } catch (Exception e) {
            return Result.error(safeErrorMessage(e, "更新账号状态失败，请稍后重试"));
        }
    }

    @Operation(summary = "管理员重置用户密码")
    @PutMapping("/admin/{id}/password")
    public Result<Void> adminResetUserPassword(
            @Parameter(description = "用户ID") @PathVariable Long id,
            @Valid @RequestBody AdminUserPasswordResetCommandDTO command) {
        try {
            UserDetailResponseDTO operator = requireAdmin();
            userService.adminResetPassword(operator.getId(), id, command);
            return Result.success();
        } catch (Exception e) {
            return Result.error(safeErrorMessage(e, "重置密码失败，请稍后重试"));
        }
    }

    @Operation(summary = "管理员获取用户签到日期")
    @GetMapping("/admin/checkins/{id}")
    public Result<List<LocalDate>> getAdminUserCheckins(
            @Parameter(description = "用户ID") @PathVariable Long id,
            @RequestParam(required = false) Integer year,
            @RequestParam(required = false) Integer month) {
        try {
            requireAdmin();
            return Result.success(userService.listCheckinDatesByMonthForAdmin(id, year, month));
        } catch (Exception e) {
            return Result.error(safeErrorMessage(e, "获取签到记录失败，请稍后重试"));
        }
    }

    @Operation(summary = "管理员获取用户答题历史")
    @GetMapping("/admin/quiz-history/{id}")
    public Result<Page<org.example.springboot.dto.response.QuizHistoryItemDTO>> getAdminUserQuizHistory(
            @Parameter(description = "用户ID") @PathVariable Long id,
            @RequestParam(defaultValue = "1") Long current,
            @RequestParam(defaultValue = "10") Long size) {
        try {
            requireAdmin();
            userService.getUserById(id);
            return Result.success(quizService.pageHistory(id, current, size));
        } catch (Exception e) {
            return Result.error(safeErrorMessage(e, "获取答题记录失败，请稍后重试"));
        }
    }

    @Operation(summary = "领取今日调车观察挑战奖励")
    @PostMapping("/rewards/shunting")
    public Result<UserDetailResponseDTO> claimShuntingReward() {
        try {
            UserDetailResponseDTO currentUser = JwtTokenUtils.getCurrentUser();
            if (currentUser == null) {
                return Result.error("未登录或登录已过期");
            }

            UserDetailResponseDTO response = userService.claimShuntingReward(currentUser.getId());
            return Result.success("今日挑战奖励领取成功", response);
        } catch (Exception e) {
            log.warn("调车挑战奖励领取失败: {}", e.getMessage());
            return Result.error(safeErrorMessage(e, "奖励领取失败，请稍后重试"));
        }
    }

    /**
     * 获取用户气血能量等级信息
     */
    @Operation(summary = "获取用户气血能量等级信息")
    @GetMapping("/level/{id}")
    public Result<UserLevelInfoResponseDTO> getUserLevelInfo(
            @Parameter(description = "用户ID") @PathVariable Long id) {
        
        try {
            UserDetailResponseDTO currentUser = JwtTokenUtils.getCurrentUser();
            if (currentUser == null) {
                return Result.error("未登录或登录已过期");
            }
            
            // 只能查看自己的等级信息
            if (!currentUser.getId().equals(id)) {
                return Result.error("无权限查看其他用户的等级信息");
            }
            
            log.info("获取用户等级信息: userId={}", id);
            UserLevelInfoResponseDTO response = userService.getUserLevelInfo(id);
            return Result.success(response);
        } catch (Exception e) {
            log.error("获取用户等级信息失败: userId={}", id, e);
            return Result.error(safeErrorMessage(e, "获取等级信息失败，请稍后重试"));
        }
    }

    @Operation(summary = "获取用户探险地图解锁状态")
    @GetMapping("/adventure-map/{id}")
    public Result<UserAdventureMapResponseDTO> getUserAdventureMap(
            @Parameter(description = "用户ID") @PathVariable Long id) {
        try {
            UserDetailResponseDTO currentUser = JwtTokenUtils.getCurrentUser();
            if (currentUser == null) {
                return Result.error("未登录或登录已过期");
            }
            if (!currentUser.getId().equals(id)) {
                return Result.error("无权查看其他用户的探险地图");
            }
            return Result.success(userService.getUserAdventureMap(id));
        } catch (Exception e) {
            log.error("获取用户探险地图失败: userId={}", id, e);
            return Result.error(safeErrorMessage(e, "获取探险地图失败"));
        }
    }

    /**
     * 上传九级荣誉证书图片（前端 html2canvas 生成），保存至 bussiness/user_avatar 并写入 user.honor
     */
    @Operation(summary = "上传九级荣誉证书")
    @PostMapping("/certificate")
    public Result<UserDetailResponseDTO> uploadCertificate(
            @Parameter(description = "证书图片") @RequestParam("file") MultipartFile file) {
        try {
            UserDetailResponseDTO currentUser = JwtTokenUtils.getCurrentUser();
            if (currentUser == null) {
                return Result.error("未登录或登录已过期");
            }
            Long userId = currentUser.getId();
            log.info("上传荣誉证书: userId={}", userId);
            UserDetailResponseDTO response = userService.uploadLevelCertificate(userId, file);
            return Result.success("证书已保存", response);
        } catch (Exception e) {
            log.error("上传荣誉证书失败", e);
            return Result.error(safeErrorMessage(e, "上传失败"));
        }
    }

    @Operation(summary = "今日签到（每天一次，+5分）")
    @PostMapping("/checkin/today")
    public Result<UserCheckinResultDTO> checkinToday() {
        try {
            UserDetailResponseDTO currentUser = JwtTokenUtils.getCurrentUser();
            if (currentUser == null) {
                return Result.error("未登录或登录已过期");
            }
            UserCheckinResultDTO response = userService.checkinToday(currentUser.getId());
            return Result.success("签到成功，气血 +5", response);
        } catch (Exception e) {
            log.error("签到失败", e);
            return Result.error(safeErrorMessage(e, "签到失败"));
        }
    }

    @Operation(summary = "获取某月签到日期")
    @GetMapping("/checkin/month")
    public Result<List<LocalDate>> getCheckinMonth(
            @Parameter(description = "年份，如 2026") @RequestParam(required = false) Integer year,
            @Parameter(description = "月份，如 4") @RequestParam(required = false) Integer month) {
        try {
            UserDetailResponseDTO currentUser = JwtTokenUtils.getCurrentUser();
            if (currentUser == null) {
                return Result.error("未登录或登录已过期");
            }
            List<LocalDate> dates = userService.listCheckinDatesByMonth(currentUser.getId(), year, month);
            return Result.success(dates);
        } catch (Exception e) {
            log.error("获取签到日历失败", e);
            return Result.error(safeErrorMessage(e, "获取签到日历失败"));
        }
    }

    @Operation(summary = "获取我的徽章")
    @GetMapping("/badges")
    public Result<List<BadgeResponseDTO>> getMyBadges() {
        try {
            UserDetailResponseDTO currentUser = JwtTokenUtils.getCurrentUser();
            if (currentUser == null) {
                return Result.error("未登录或登录已过期");
            }
            return Result.success(userService.listUserBadges(currentUser.getId()));
        } catch (Exception e) {
            log.error("获取徽章失败", e);
            return Result.error(safeErrorMessage(e, "获取徽章失败"));
        }
    }

    private String safeErrorMessage(Exception error, String fallback) {
        if (error instanceof BusinessException || error instanceof ServiceException) {
            return error.getMessage();
        }
        return fallback;
    }

    private UserDetailResponseDTO requireAdmin() {
        UserDetailResponseDTO currentUser = JwtTokenUtils.getCurrentUser();
        if (currentUser == null || !UserType.ADMIN.getCode().equals(currentUser.getUserType())) {
            throw new BusinessException("权限不足");
        }
        return currentUser;
    }

    private String getClientAddress(HttpServletRequest request) {
        String realIp = request.getHeader("X-Real-IP");
        if (realIp != null && !realIp.isBlank()) {
            return realIp.trim();
        }
        String forwarded = request.getHeader("X-Forwarded-For");
        if (forwarded != null && !forwarded.isBlank()) {
            return forwarded.split(",", 2)[0].trim();
        }
        return request.getRemoteAddr();
    }

}
