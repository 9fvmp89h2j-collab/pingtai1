package org.example.springboot.service;

import jakarta.annotation.Resource;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
public class AuthSessionService {
    @Resource
    private JdbcTemplate jdbcTemplate;

    @Resource
    private AuditEventService auditEventService;

    @Transactional
    public void create(Long userId, String tokenId, LocalDateTime expiresAt) {
        jdbcTemplate.update("""
                INSERT INTO auth_session (user_id, token_id, issued_at, expires_at, last_seen_at)
                VALUES (?, ?, CURRENT_TIMESTAMP, ?, CURRENT_TIMESTAMP)
                """, userId, tokenId, expiresAt);
        auditEventService.record(userId, null, "LOGIN", "AUTH_SESSION", tokenId,
                "SUCCESS", java.util.Map.of("expiresAt", expiresAt.toString()));
    }

    public boolean isActive(String tokenId) {
        if (tokenId == null || tokenId.isBlank()) {
            return true;
        }
        Integer count = jdbcTemplate.queryForObject("""
                SELECT COUNT(*) FROM auth_session
                WHERE token_id = ? AND revoked_at IS NULL AND expires_at > CURRENT_TIMESTAMP
                """, Integer.class, tokenId);
        return count != null && count > 0;
    }

    @Transactional
    public void touch(String tokenId) {
        if (tokenId == null || tokenId.isBlank()) {
            return;
        }
        jdbcTemplate.update("UPDATE auth_session SET last_seen_at = CURRENT_TIMESTAMP WHERE token_id = ? AND revoked_at IS NULL", tokenId);
    }

    @Transactional
    public void revoke(Long userId, String tokenId, String reason) {
        if (tokenId == null || tokenId.isBlank()) {
            return;
        }
        int updated = jdbcTemplate.update("""
                UPDATE auth_session
                SET revoked_at = CURRENT_TIMESTAMP, revoke_reason = ?
                WHERE user_id = ? AND token_id = ? AND revoked_at IS NULL
                """, reason, userId, tokenId);
        if (updated > 0) {
            auditEventService.record(userId, null, "LOGOUT", "AUTH_SESSION", tokenId,
                    "SUCCESS", java.util.Map.of("reason", reason == null ? "user_request" : reason));
        }
    }
}
