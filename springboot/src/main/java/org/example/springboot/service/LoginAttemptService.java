package org.example.springboot.service;

import org.example.springboot.exception.BusinessException;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;
import java.util.Locale;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class LoginAttemptService {

    private static final int MAX_FAILURES = 5;
    private static final Duration WINDOW = Duration.ofMinutes(15);
    private final ConcurrentHashMap<String, Attempt> attempts = new ConcurrentHashMap<>();

    public void checkAllowed(String remoteAddress, String username) {
        String key = key(remoteAddress, username);
        Attempt attempt = attempts.get(key);
        if (attempt == null) return;
        if (Instant.now().isAfter(attempt.firstFailure().plus(WINDOW))) {
            attempts.remove(key);
            return;
        }
        if (attempt.failures() >= MAX_FAILURES) {
            throw new BusinessException("登录尝试过多，请15分钟后再试");
        }
    }

    public void recordFailure(String remoteAddress, String username) {
        String key = key(remoteAddress, username);
        Instant now = Instant.now();
        attempts.compute(key, (_ignored, current) -> {
            if (current == null || now.isAfter(current.firstFailure().plus(WINDOW))) {
                return new Attempt(1, now);
            }
            return new Attempt(current.failures() + 1, current.firstFailure());
        });
    }

    public void clear(String remoteAddress, String username) {
        attempts.remove(key(remoteAddress, username));
    }

    private String key(String remoteAddress, String username) {
        String address = remoteAddress == null ? "unknown" : remoteAddress.trim();
        String identity = username == null ? "" : username.trim().toLowerCase(Locale.ROOT);
        return address + "\n" + identity;
    }

    private record Attempt(int failures, Instant firstFailure) {
    }
}
