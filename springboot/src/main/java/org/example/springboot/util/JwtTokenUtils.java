package org.example.springboot.util;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.interfaces.DecodedJWT;
import lombok.extern.slf4j.Slf4j;
import org.example.springboot.dto.response.UserDetailResponseDTO;
import org.springframework.util.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import jakarta.servlet.http.HttpServletRequest;
import java.util.Date;
import java.util.UUID;

/**
 * JWT工具类 - 用于JWT token的生成、验证和用户信息获取
 *
 * 主要功能：
 * 1. 生成JWT token（包含userId、username、roleType）
 * 2. 验证JWT token有效性和过期检查
 * 3. 从请求属性中获取当前用户信息（由JwtAuthenticationFilter设置）
 *
 * 使用说明：
 * - Token的解析和提取由JwtAuthenticationFilter负责
 * - Controller中使用getCurrentUser()和getCurrentUserId()获取当前用户信息
 * - 不要直接操作token，所有token相关逻辑在Filter中处理
 *
 * Token传输规范：
 * - 只支持标准的 Authorization: Bearer <token> 方式
 *
 * 安全特性：
 * - 使用HMAC256算法签名
 * - Token有效期7天
 * - 完善的异常处理和日志记录
 */
@Slf4j
@Component
public class JwtTokenUtils {

    /**
     * JWT密钥
     */
    private static String secret;

    /**
     * Token过期时间（7天）
     */
    private static long expireTime;

    /**
     * Token发行者
     */
    private static final String ISSUER = "xinglin-detective-platform";

    @Value("${jwt.secret}")
    void configureSecret(String configuredSecret) {
        if (!StringUtils.hasText(configuredSecret) || configuredSecret.length() < 32) {
            throw new IllegalStateException("JWT_SECRET must contain at least 32 characters");
        }
        secret = configuredSecret;
    }

    @Value("${jwt.expiration:86400000}")
    void configureExpiration(long configuredExpiration) {
        if (configuredExpiration <= 0) {
            throw new IllegalStateException("JWT_EXPIRATION must be positive");
        }
        expireTime = configuredExpiration;
    }

    /**
     * 生成JWT token
     * @param userId 用户ID
     * @param username 用户名
     * @param roleType 角色代码
     * @return JWT token
     */
    public static String generateToken(Long userId, String username, String roleType) {
        return generateToken(userId, username, roleType, UUID.randomUUID().toString());
    }

    public static String generateToken(Long userId, String username, String roleType, String tokenId) {
        try {
            Algorithm algorithm = Algorithm.HMAC256(secret);
            Date expireDate = new Date(System.currentTimeMillis() + expireTime);

            return JWT.create()
                    .withClaim("userId", userId)
                    .withClaim("username", username)
                    .withClaim("roleType", roleType)
                    .withJWTId(tokenId)
                    .withExpiresAt(expireDate)
                    .withIssuedAt(new Date())
                    .withIssuer(ISSUER)
                    .sign(algorithm);
        } catch (Exception e) {
            log.error("生成JWT token失败", e);
            throw new RuntimeException("生成JWT token失败", e);
        }
    }

    public static long getExpirationMillis() {
        return expireTime;
    }

    /**
     * 验证JWT token有效性
     * @param token JWT token
     * @return 解码后的JWT
     * @throws JWTVerificationException token验证失败
     */
    public static DecodedJWT verifyToken(String token) throws JWTVerificationException {
        Algorithm algorithm = Algorithm.HMAC256(secret);
        JWTVerifier verifier = JWT.require(algorithm)
                .withIssuer(ISSUER)
                .build();
        return verifier.verify(token);
    }

    /**
     * 检查token是否过期
     * @param token JWT token
     * @return 是否过期
     */
    public static boolean isTokenExpired(String token) {
        try {
            DecodedJWT jwt = verifyToken(token);
            return jwt.getExpiresAt().before(new Date());
        } catch (Exception e) {
            return true;
        }
    }

    /**
     * 获取当前请求的用户ID（从RequestContextHolder获取）
     * @return 当前用户ID，获取失败返回null
     */
    public static Long getCurrentUserId() {
        try {
            ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            if (attributes != null) {
                HttpServletRequest request = attributes.getRequest();
                Object userId = request.getAttribute("currentUserId");
                if (userId instanceof Long) {
                    return (Long) userId;
                }
            }
        } catch (Exception e) {
            log.error("获取当前用户ID失败", e);
        }
        return null;
    }

    /**
     * 获取当前请求的用户信息（从请求属性中获取）
     * 注意：此方法依赖于JwtAuthenticationFilter设置的请求属性
     * @return 当前用户对象，获取失败返回null
     */
    public static UserDetailResponseDTO getCurrentUser() {
        try {
            ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            if (attributes != null) {
                HttpServletRequest request = attributes.getRequest();
                return (UserDetailResponseDTO)request.getAttribute("currentUser");
            }
        } catch (Exception e) {
            log.error("获取当前用户对象失败", e);
        }
        return null;
    }

    /**
     * 获取当前用户ID的字符串形式
     * @return 当前用户ID字符串，获取失败返回null
     */
    public static String getCurrentUserIdAsString() {
        Long userId = getCurrentUserId();
        return userId != null ? String.valueOf(userId) : null;
    }

    public static String getCurrentTokenId() {
        try {
            ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            if (attributes != null) {
                Object tokenId = attributes.getRequest().getAttribute("currentTokenId");
                return tokenId == null ? null : String.valueOf(tokenId);
            }
        } catch (Exception e) {
            log.error("获取当前会话ID失败", e);
        }
        return null;
    }

    /**
     * 获取当前用户类型/角色
     * @return 当前用户类型，获取失败返回null
     */
    public static String getCurrentUserType() {
        UserDetailResponseDTO currentUser = getCurrentUser();
        if(currentUser==null)return null;
        return currentUser.getUserType();

    }

    /**
     * 判断当前用户是否为管理员
     * @return 是否为管理员
     */
    public static boolean isAdmin() {


                return "ADMIN".equals(getCurrentUserType());


    }
}
