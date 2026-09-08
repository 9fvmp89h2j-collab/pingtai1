package org.example.springboot.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.Resource;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

/** 统一业务写入审计，不保存密码、Token 或验证码。 */
@Service
public class AuditEventService {
    @Resource
    private JdbcTemplate jdbcTemplate;

    @Resource
    private ObjectMapper objectMapper;

    public void record(Long actorUserId,
                       String requestId,
                       String action,
                       String resourceType,
                       String resourceId,
                       String outcome,
                       Object details) {
        try {
            String detailsJson = details == null ? null : objectMapper.writeValueAsString(details);
            jdbcTemplate.update("""
                    INSERT INTO audit_event
                    (actor_user_id, request_id, action, resource_type, resource_id, outcome, details_json)
                    VALUES (?, ?, ?, ?, ?, ?, ?)
                    """, actorUserId, requestId, action, resourceType, resourceId, outcome, detailsJson);
        } catch (Exception e) {
            throw new IllegalStateException("保存审计事件失败", e);
        }
    }
}
