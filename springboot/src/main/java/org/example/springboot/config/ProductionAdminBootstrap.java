package org.example.springboot.config;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import org.example.springboot.entity.User;
import org.example.springboot.mapper.UserMapper;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
@Profile("prod")
public class ProductionAdminBootstrap implements ApplicationRunner {

    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;

    @Value("${bootstrap.admin.username:}")
    private String username;
    @Value("${bootstrap.admin.email:}")
    private String email;
    @Value("${bootstrap.admin.password:}")
    private String password;

    public ProductionAdminBootstrap(UserMapper userMapper, PasswordEncoder passwordEncoder) {
        this.userMapper = userMapper;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(ApplicationArguments args) {
        long adminCount = userMapper.selectCount(
                new LambdaQueryWrapper<User>().eq(User::getUserType, "ADMIN")
        );
        if (adminCount > 0) {
            return;
        }
        if (username == null || !username.matches("^[a-zA-Z0-9_]{3,50}$")) {
            throw new IllegalStateException("ADMIN_BOOTSTRAP_USERNAME must be 3-50 letters, numbers, or underscores");
        }
        if (email == null || !email.matches("^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$")) {
            throw new IllegalStateException("ADMIN_BOOTSTRAP_EMAIL must be a valid email address");
        }
        if (password == null || password.length() < 12) {
            throw new IllegalStateException("ADMIN_BOOTSTRAP_PASSWORD must be at least 12 characters");
        }

        LocalDateTime now = LocalDateTime.now();
        User admin = User.builder()
                .username(username)
                .email(email)
                .password(passwordEncoder.encode(password))
                .userType("ADMIN")
                .status(1)
                .score(0)
                .createdAt(now)
                .updatedAt(now)
                .build();
        userMapper.insert(admin);
    }
}
