package org.example.springboot.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.springboot.exception.BusinessException;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.annotation.Resource;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;
import java.util.Map;

/**
 * 统一幂等写入记录。业务事务应在外层开启，本服务只负责记录同一请求是否已经处理。
 */
@Service
public class WriteOperationService {
    @Resource
    private JdbcTemplate jdbcTemplate;

    @Resource
    private ObjectMapper objectMapper;

    @Transactional
    public Claim claim(Long userId, String operationKey, String operationType, Object request) {
        if (userId == null) {
            throw new BusinessException("401", "请先登录");
        }
        String normalizedKey = operationKey == null || operationKey.isBlank()
                ? java.util.UUID.randomUUID().toString()
                : operationKey.trim();
        if (normalizedKey.length() > 128) {
            throw new BusinessException("400", "幂等键长度不能超过128个字符");
        }
        String requestHash = hash(request);
        try {
            jdbcTemplate.update("""
                    INSERT INTO write_operation
                    (user_id, operation_key, request_hash, operation_type, status)
                    VALUES (?, ?, ?, ?, 'PROCESSING')
                    """, userId, normalizedKey, requestHash, operationType);
            return new Claim(userId, normalizedKey, requestHash, false);
        } catch (DuplicateKeyException duplicate) {
            Map<String, Object> existing = jdbcTemplate.queryForMap("""
                    SELECT request_hash, status
                    FROM write_operation
                    WHERE user_id = ? AND operation_key = ?
                    """, userId, normalizedKey);
            if (!requestHash.equals(existing.get("request_hash"))) {
                throw new BusinessException("409", "同一幂等键不能提交不同内容");
            }
            String status = String.valueOf(existing.get("status"));
            if (!"COMPLETED".equals(status)) {
                throw new BusinessException("409", "该请求正在处理中，请勿重复提交");
            }
            return new Claim(userId, normalizedKey, requestHash, true);
        }
    }

    public void complete(Claim claim, Object response) {
        try {
            String responseJson = objectMapper.writeValueAsString(response);
            jdbcTemplate.update("""
                    UPDATE write_operation
                    SET status = 'COMPLETED', response_json = ?, completed_at = CURRENT_TIMESTAMP
                    WHERE user_id = ? AND operation_key = ? AND request_hash = ?
                    """, responseJson, claim.userId(), claim.operationKey(), claim.requestHash());
        } catch (Exception e) {
            throw new IllegalStateException("保存幂等操作结果失败", e);
        }
    }

    public String hash(Object request) {
        try {
            String canonical = objectMapper.writeValueAsString(request);
            return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256")
                    .digest(canonical.getBytes(StandardCharsets.UTF_8)));
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("系统不支持SHA-256", e);
        } catch (Exception e) {
            throw new IllegalArgumentException("无法计算请求摘要", e);
        }
    }

    public record Claim(Long userId, String operationKey, String requestHash, boolean replay) {
    }
}
