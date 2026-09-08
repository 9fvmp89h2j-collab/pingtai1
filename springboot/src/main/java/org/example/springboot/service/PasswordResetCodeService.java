package org.example.springboot.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.example.springboot.entity.User;
import org.example.springboot.exception.BusinessException;
import org.example.springboot.mapper.UserMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.time.Duration;
import java.time.Instant;
import java.util.Locale;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Service
public class PasswordResetCodeService {

    private static final Duration CODE_TTL = Duration.ofMinutes(10);
    private static final Duration RESEND_INTERVAL = Duration.ofSeconds(60);
    private static final int MAX_ATTEMPTS = 5;

    @Resource
    private UserMapper userMapper;

    @Resource
    private JavaMailSender mailSender;

    @Value("${spring.mail.username:}")
    private String sender;

    private final SecureRandom random = new SecureRandom();
    private final ConcurrentHashMap<String, ResetCode> codes = new ConcurrentHashMap<>();

    public void sendCode(String username, String email) {
        if (sender == null || sender.isBlank()) {
            throw new BusinessException("邮件服务尚未配置，请联系管理员");
        }
        String key = key(username, email);
        ResetCode previous = codes.get(key);
        Instant now = Instant.now();
        if (previous != null && now.isBefore(previous.sentAt().plus(RESEND_INTERVAL))) {
            throw new BusinessException("验证码发送过于频繁，请稍后再试");
        }

        User user = userMapper.selectOne(new LambdaQueryWrapper<User>()
                .eq(User::getUsername, username)
                .eq(User::getEmail, email));
        // 对不存在的账号返回相同结果，避免公开接口泄露注册信息。
        if (user == null) {
            log.info("密码重置验证码请求未匹配账号");
            return;
        }
        String code = String.format("%06d", random.nextInt(1_000_000));
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(sender);
        message.setTo(email);
        message.setSubject("小铜人中医侦探社 - 密码重置验证码");
        message.setText("你的密码重置验证码是：" + code
                + "\n\n验证码10分钟内有效，请勿转发给他人。如非本人操作，请忽略此邮件。");
        mailSender.send(message);
        codes.put(key, new ResetCode(code, now, now.plus(CODE_TTL), 0));
    }

    public void verifyAndConsume(String username, String email, String submittedCode) {
        String key = key(username, email);
        ResetCode stored = codes.get(key);
        Instant now = Instant.now();
        if (stored == null || now.isAfter(stored.expiresAt())) {
            codes.remove(key);
            throw new BusinessException("验证码无效或已过期");
        }
        if (stored.attempts() >= MAX_ATTEMPTS) {
            codes.remove(key);
            throw new BusinessException("验证码尝试次数过多，请重新获取");
        }
        if (!stored.code().equals(submittedCode)) {
            codes.put(key, new ResetCode(stored.code(), stored.sentAt(), stored.expiresAt(), stored.attempts() + 1));
            throw new BusinessException("验证码无效或已过期");
        }
        codes.remove(key);
    }

    private String key(String username, String email) {
        return username.trim().toLowerCase(Locale.ROOT) + "\n" + email.trim().toLowerCase(Locale.ROOT);
    }

    private record ResetCode(String code, Instant sentAt, Instant expiresAt, int attempts) {
    }
}
