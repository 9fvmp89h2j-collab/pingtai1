package org.example.springboot.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.Resource;
import org.example.springboot.dto.command.CopperAcupointDraftCommandDTO;
import org.example.springboot.dto.command.CopperStoryDraftCommandDTO;
import org.example.springboot.dto.command.CopperStoryProgressCommandDTO;
import org.example.springboot.exception.BusinessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

@Service
public class CopperContentService {
    private static final Set<String> ACTIVE_DRAFT_STATUSES = Set.of("DRAFT", "IN_REVIEW", "APPROVED", "SCHEDULED");
    private static final Set<String> TRANSITION_TYPES = Set.of("ACUPOINT", "STORY");
    private static final String CHILD_SAFETY_TIP = "只看3D铜人和文化图卡，不做身体操作；有问题请告诉老师或家长。";

    @Resource
    private JdbcTemplate jdbcTemplate;
    @Resource
    private ObjectMapper objectMapper;
    @Resource
    private AuditEventService auditEventService;

    public Map<String, Object> overview() {
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("acupointTotal", scalar("SELECT COUNT(*) FROM acupoint_knowledge"));
        result.put("modeledTotal", scalar("""
                SELECT COUNT(*) FROM acupoint_knowledge
                WHERE enabled = 1 AND position_x IS NOT NULL AND position_y IS NOT NULL AND position_z IS NOT NULL
                """));
        result.put("storyPublished", scalar("SELECT COUNT(*) FROM copper_story_revision WHERE status = 'PUBLISHED'"));
        result.put("pendingReview", scalar("""
                SELECT (SELECT COUNT(*) FROM copper_acupoint_revision WHERE status = 'IN_REVIEW')
                     + (SELECT COUNT(*) FROM copper_story_revision WHERE status = 'IN_REVIEW')
                """));
        result.put("scheduled", scalar("""
                SELECT (SELECT COUNT(*) FROM copper_acupoint_revision WHERE status = 'SCHEDULED')
                     + (SELECT COUNT(*) FROM copper_story_revision WHERE status = 'SCHEDULED')
                """));
        result.put("releaseTotal", scalar("SELECT COUNT(*) FROM copper_content_release"));
        return result;
    }

    public Map<String, Object> listAcupoints(int page, int size, String keyword, String status, Boolean modeled) {
        int safePage = Math.max(page, 1);
        int safeSize = Math.min(Math.max(size, 1), 100);
        String normalizedStatus = normalizeOptionalStatus(status);
        String like = keyword == null || keyword.isBlank() ? null : "%" + keyword.trim() + "%";
        String sql = """
                SELECT r.id revision_id, r.acupoint_code code, r.version_no, r.status,
                       JSON_UNQUOTE(JSON_EXTRACT(r.payload_json, '$.name')) name,
                       JSON_UNQUOTE(JSON_EXTRACT(r.payload_json, '$.pinyin')) pinyin,
                       JSON_UNQUOTE(JSON_EXTRACT(r.payload_json, '$.meridianName')) meridian_name,
                       JSON_UNQUOTE(JSON_EXTRACT(r.payload_json, '$.bodyArea')) body_area,
                       JSON_UNQUOTE(JSON_EXTRACT(r.payload_json, '$.childLocation')) child_location,
                       JSON_UNQUOTE(JSON_EXTRACT(r.payload_json, '$.childDescription')) child_description,
                       JSON_UNQUOTE(JSON_EXTRACT(r.payload_json, '$.childTraditionalUse')) child_traditional_use,
                       JSON_UNQUOTE(JSON_EXTRACT(r.payload_json, '$.sourceName')) source_name,
                       CASE WHEN JSON_EXTRACT(r.payload_json, '$.positionX') IS NOT NULL
                                  AND JSON_EXTRACT(r.payload_json, '$.positionY') IS NOT NULL
                                  AND JSON_EXTRACT(r.payload_json, '$.positionZ') IS NOT NULL THEN 1 ELSE 0 END modeled,
                       r.updated_at, r.scheduled_at, r.published_at, r.publish_error
                FROM copper_acupoint_revision r
                JOIN (SELECT acupoint_code, MAX(version_no) version_no
                      FROM copper_acupoint_revision GROUP BY acupoint_code) latest
                  ON latest.acupoint_code = r.acupoint_code AND latest.version_no = r.version_no
                WHERE (? IS NULL OR r.acupoint_code LIKE ?
                       OR JSON_UNQUOTE(JSON_EXTRACT(r.payload_json, '$.name')) LIKE ?
                       OR JSON_UNQUOTE(JSON_EXTRACT(r.payload_json, '$.meridianName')) LIKE ?)
                  AND (? IS NULL OR r.status = ?)
                  AND (? IS NULL OR (CASE WHEN JSON_EXTRACT(r.payload_json, '$.positionX') IS NOT NULL
                                                AND JSON_EXTRACT(r.payload_json, '$.positionY') IS NOT NULL
                                                AND JSON_EXTRACT(r.payload_json, '$.positionZ') IS NOT NULL THEN 1 ELSE 0 END) = ?)
                ORDER BY CAST(JSON_UNQUOTE(JSON_EXTRACT(r.payload_json, '$.sortOrder')) AS UNSIGNED), r.acupoint_code
                LIMIT ? OFFSET ?
                """;
        List<Map<String, Object>> records = jdbcTemplate.queryForList(sql,
                like, like, like, like, normalizedStatus, normalizedStatus,
                modeled, modeled, safeSize, (safePage - 1) * safeSize);
        String countSql = """
                SELECT COUNT(*) FROM copper_acupoint_revision r
                JOIN (SELECT acupoint_code, MAX(version_no) version_no
                      FROM copper_acupoint_revision GROUP BY acupoint_code) latest
                  ON latest.acupoint_code = r.acupoint_code AND latest.version_no = r.version_no
                WHERE (? IS NULL OR r.acupoint_code LIKE ?
                       OR JSON_UNQUOTE(JSON_EXTRACT(r.payload_json, '$.name')) LIKE ?
                       OR JSON_UNQUOTE(JSON_EXTRACT(r.payload_json, '$.meridianName')) LIKE ?)
                  AND (? IS NULL OR r.status = ?)
                  AND (? IS NULL OR (CASE WHEN JSON_EXTRACT(r.payload_json, '$.positionX') IS NOT NULL
                                                AND JSON_EXTRACT(r.payload_json, '$.positionY') IS NOT NULL
                                                AND JSON_EXTRACT(r.payload_json, '$.positionZ') IS NOT NULL THEN 1 ELSE 0 END) = ?)
                """;
        Integer total = jdbcTemplate.queryForObject(countSql, Integer.class,
                like, like, like, like, normalizedStatus, normalizedStatus, modeled, modeled);
        return page(records, safePage, safeSize, total);
    }

    public Map<String, Object> getAcupoint(String code) {
        Map<String, Object> row = latestRevision("copper_acupoint_revision", "acupoint_code", normalizeCode(code));
        Map<String, Object> payload = readJson(row.get("payload_json"));
        payload.put("revisionId", row.get("id"));
        payload.put("versionNo", row.get("version_no"));
        payload.put("status", row.get("status"));
        payload.put("scheduledAt", row.get("scheduled_at"));
        payload.put("publishedAt", row.get("published_at"));
        payload.put("validation", validateAcupoint(payload));
        return payload;
    }

    @Transactional
    public Map<String, Object> saveAcupointDraft(String rawCode, CopperAcupointDraftCommandDTO command, Long operatorId) {
        String code = normalizeCode(rawCode == null || rawCode.isBlank() ? command.getCode() : rawCode);
        Map<String, Object> payload = objectMapper.convertValue(command, new TypeReference<>() {});
        payload.put("code", code);
        if (payload.get("enabled") == null) payload.put("enabled", true);
        List<String> errors = validateAcupoint(payload);
        if (!errors.isEmpty()) throw new BusinessException("400", String.join("；", errors));
        Long revisionId = saveDraft("copper_acupoint_revision", "acupoint_code", code, payload,
                null, null, operatorId);
        audit(operatorId, "COPPER_ACUPOINT_SAVE_DRAFT", "ACUPOINT", code, Map.of("revisionId", revisionId));
        return getAcupoint(code);
    }

    public Map<String, Object> listStories(int page, int size, String keyword, String status) {
        int safePage = Math.max(page, 1);
        int safeSize = Math.min(Math.max(size, 1), 100);
        String normalizedStatus = normalizeOptionalStatus(status);
        String like = keyword == null || keyword.isBlank() ? null : "%" + keyword.trim() + "%";
        List<Map<String, Object>> records = jdbcTemplate.queryForList("""
                SELECT r.id revision_id, r.story_code, r.version_no, r.status, r.title, r.summary,
                       r.cover_path, r.updated_at, r.scheduled_at, r.published_at, r.publish_error,
                       JSON_LENGTH(JSON_EXTRACT(r.payload_json, '$.pages')) page_count,
                       JSON_LENGTH(JSON_EXTRACT(r.payload_json, '$.clues')) clue_count
                FROM copper_story_revision r
                JOIN (SELECT story_code, MAX(version_no) version_no
                      FROM copper_story_revision GROUP BY story_code) latest
                  ON latest.story_code = r.story_code AND latest.version_no = r.version_no
                WHERE (? IS NULL OR r.story_code LIKE ? OR r.title LIKE ?)
                  AND (? IS NULL OR r.status = ?)
                ORDER BY r.updated_at DESC
                LIMIT ? OFFSET ?
                """, like, like, like, normalizedStatus, normalizedStatus, safeSize, (safePage - 1) * safeSize);
        Integer total = jdbcTemplate.queryForObject("""
                SELECT COUNT(*) FROM copper_story_revision r
                JOIN (SELECT story_code, MAX(version_no) version_no
                      FROM copper_story_revision GROUP BY story_code) latest
                  ON latest.story_code = r.story_code AND latest.version_no = r.version_no
                WHERE (? IS NULL OR r.story_code LIKE ? OR r.title LIKE ?)
                  AND (? IS NULL OR r.status = ?)
                """, Integer.class, like, like, like, normalizedStatus, normalizedStatus);
        return page(records, safePage, safeSize, total);
    }

    public Map<String, Object> getAdminStory(String code) {
        Map<String, Object> row = latestRevision("copper_story_revision", "story_code", normalizeStoryCode(code));
        Map<String, Object> payload = readJson(row.get("payload_json"));
        payload.put("revisionId", row.get("id"));
        payload.put("versionNo", row.get("version_no"));
        payload.put("status", row.get("status"));
        payload.put("scheduledAt", row.get("scheduled_at"));
        payload.put("publishedAt", row.get("published_at"));
        payload.put("validation", validateStory(payload));
        return payload;
    }

    @Transactional
    public Map<String, Object> saveStoryDraft(String rawCode, CopperStoryDraftCommandDTO command, Long operatorId) {
        String code = normalizeStoryCode(rawCode == null || rawCode.isBlank() ? command.getStoryCode() : rawCode);
        Map<String, Object> payload = objectMapper.convertValue(command, new TypeReference<>() {});
        payload.put("storyCode", code);
        List<String> errors = validateStory(payload);
        if (!errors.isEmpty()) throw new BusinessException("400", String.join("；", errors));
        Long revisionId = saveDraft("copper_story_revision", "story_code", code, payload,
                string(payload.get("title")), string(payload.get("summary")), operatorId);
        audit(operatorId, "COPPER_STORY_SAVE_DRAFT", "STORY", code, Map.of("revisionId", revisionId));
        return getAdminStory(code);
    }

    @Transactional
    public Map<String, Object> transition(String type, String rawKey, String action,
                                          LocalDateTime scheduledAt, Long operatorId) {
        String normalizedType = normalizeType(type);
        String key = "ACUPOINT".equals(normalizedType) ? normalizeCode(rawKey) : normalizeStoryCode(rawKey);
        String table = tableFor(normalizedType);
        String keyColumn = keyColumnFor(normalizedType);
        Map<String, Object> row = latestRevision(table, keyColumn, key);
        long revisionId = number(row.get("id")).longValue();
        String current = string(row.get("status"));
        String normalizedAction = action == null ? "" : action.trim().toUpperCase(Locale.ROOT);
        switch (normalizedAction) {
            case "SUBMIT" -> updateStatus(table, revisionId, current, "DRAFT", "IN_REVIEW", operatorId, null);
            case "APPROVE" -> updateStatus(table, revisionId, current, "IN_REVIEW", "APPROVED", operatorId, null);
            case "SCHEDULE" -> {
                if (scheduledAt == null || !scheduledAt.isAfter(LocalDateTime.now())) {
                    throw new BusinessException("400", "定时发布时间必须晚于当前时间");
                }
                updateStatus(table, revisionId, current, "APPROVED", "SCHEDULED", operatorId, scheduledAt);
            }
            case "PUBLISH" -> {
                if ("PUBLISHED".equals(current)) break;
                if (!Set.of("APPROVED", "SCHEDULED").contains(current)) {
                    throw new BusinessException("409", "只有已审核内容可以发布");
                }
                publishRevision(normalizedType, row, operatorId);
            }
            case "ARCHIVE" -> archiveRevision(normalizedType, row, operatorId);
            default -> throw new BusinessException("400", "未知的内容状态操作");
        }
        audit(operatorId, "COPPER_CONTENT_" + normalizedAction, normalizedType, key,
                Map.of("revisionId", revisionId));
        return "ACUPOINT".equals(normalizedType) ? getAcupoint(key) : getAdminStory(key);
    }

    public List<Map<String, Object>> revisions(String type, String rawKey) {
        String normalizedType = normalizeType(type);
        String key = "ACUPOINT".equals(normalizedType) ? normalizeCode(rawKey) : normalizeStoryCode(rawKey);
        return jdbcTemplate.queryForList("SELECT id, version_no, status, created_by, reviewed_by, published_by, " +
                        "created_at, updated_at, reviewed_at, scheduled_at, published_at, publish_error FROM " + tableFor(normalizedType) +
                        " WHERE " + keyColumnFor(normalizedType) + " = ? ORDER BY version_no DESC", key);
    }

    @Transactional
    public Map<String, Object> restoreRevision(String type, String rawKey, int version, Long operatorId) {
        String normalizedType = normalizeType(type);
        String key = "ACUPOINT".equals(normalizedType) ? normalizeCode(rawKey) : normalizeStoryCode(rawKey);
        String table = tableFor(normalizedType);
        String keyColumn = keyColumnFor(normalizedType);
        List<Map<String, Object>> sourceRows = jdbcTemplate.queryForList(
                "SELECT * FROM " + table + " WHERE " + keyColumn + " = ? AND version_no = ? LIMIT 1", key, version);
        if (sourceRows.isEmpty()) throw new BusinessException("404", "指定版本不存在");
        Map<String, Object> source = sourceRows.get(0);
        Map<String, Object> payload = readJson(source.get("payload_json"));
        Long revisionId = saveDraft(table, keyColumn, key, payload,
                "STORY".equals(normalizedType) ? string(source.get("title")) : null,
                "STORY".equals(normalizedType) ? string(source.get("summary")) : null,
                operatorId);
        audit(operatorId, "COPPER_CONTENT_RESTORE_VERSION", normalizedType, key,
                Map.of("sourceVersion", version, "revisionId", revisionId));
        return "ACUPOINT".equals(normalizedType) ? getAcupoint(key) : getAdminStory(key);
    }

    public List<Map<String, Object>> publicStories(Long userId) {
        List<Map<String, Object>> rows = jdbcTemplate.queryForList("""
                SELECT story_code, version_no, title, summary, cover_path, payload_json, published_at
                FROM copper_story_revision WHERE status = 'PUBLISHED'
                ORDER BY published_at DESC, id DESC
                """);
        List<Map<String, Object>> result = new ArrayList<>();
        for (Map<String, Object> row : rows) {
            Map<String, Object> payload = readJson(row.get("payload_json"));
            payload.put("versionNo", row.get("version_no"));
            payload.put("publishedAt", row.get("published_at"));
            if (userId != null) payload.put("progress", storyProgress(userId, string(row.get("story_code"))));
            result.add(payload);
        }
        return result;
    }

    public Map<String, Object> publicStory(String code, Long userId) {
        List<Map<String, Object>> rows = jdbcTemplate.queryForList("""
                SELECT version_no, payload_json, published_at FROM copper_story_revision
                WHERE story_code = ? AND status = 'PUBLISHED' ORDER BY version_no DESC LIMIT 1
                """, normalizeStoryCode(code));
        if (rows.isEmpty()) throw new BusinessException("404", "故事尚未发布");
        Map<String, Object> row = rows.get(0);
        Map<String, Object> payload = readJson(row.get("payload_json"));
        payload.put("versionNo", row.get("version_no"));
        payload.put("publishedAt", row.get("published_at"));
        if (userId != null) payload.put("progress", storyProgress(userId, code));
        return payload;
    }

    @Transactional
    public Map<String, Object> saveStoryProgress(Long userId, String code, CopperStoryProgressCommandDTO command) {
        if (userId == null) throw new BusinessException("401", "请先登录再保存故事进度");
        publicStory(code, userId);
        Map<String, Object> progress = command.getProgress() == null ? Map.of() : command.getProgress();
        boolean completed = Boolean.TRUE.equals(command.getCompleted());
        jdbcTemplate.update("""
                INSERT INTO user_copper_story_progress
                  (user_id, story_code, progress_json, completed, completed_at)
                VALUES (?, ?, ?, ?, CASE WHEN ? = 1 THEN CURRENT_TIMESTAMP ELSE NULL END)
                ON DUPLICATE KEY UPDATE progress_json = VALUES(progress_json),
                  completed = GREATEST(completed, VALUES(completed)),
                  completed_at = CASE WHEN completed_at IS NULL AND VALUES(completed) = 1
                                      THEN CURRENT_TIMESTAMP ELSE completed_at END
                """, userId, normalizeStoryCode(code), writeJson(progress), completed, completed);
        return storyProgress(userId, code);
    }

    public Map<String, Object> feed(Long userId, int limit) {
        if (userId == null) return Map.of("records", List.of(), "unreadCount", 0);
        int safeLimit = Math.min(Math.max(limit, 1), 50);
        List<Map<String, Object>> records = jdbcTemplate.queryForList("""
                SELECT r.id, r.content_type, r.content_key, r.version_no, r.title, r.summary,
                       r.route, r.published_at, CASE WHEN u.release_id IS NULL THEN 0 ELSE 1 END is_read
                FROM copper_content_release r
                LEFT JOIN user_content_release_read u ON u.release_id = r.id AND u.user_id = ?
                WHERE r.retracted_at IS NULL
                ORDER BY r.published_at DESC, r.id DESC LIMIT ?
                """, userId, safeLimit);
        Integer unread = jdbcTemplate.queryForObject("""
                SELECT COUNT(*) FROM copper_content_release r
                LEFT JOIN user_content_release_read u ON u.release_id = r.id AND u.user_id = ?
                WHERE u.release_id IS NULL AND r.retracted_at IS NULL
                """, Integer.class, userId);
        return Map.of("records", records, "unreadCount", unread == null ? 0 : unread);
    }

    @Transactional
    public void markRead(Long userId, Long releaseId) {
        if (userId == null) throw new BusinessException("401", "请先登录");
        Integer exists = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM copper_content_release WHERE id = ?",
                Integer.class, releaseId);
        if (exists == null || exists == 0) throw new BusinessException("404", "推送记录不存在");
        jdbcTemplate.update("""
                INSERT INTO user_content_release_read (user_id, release_id) VALUES (?, ?)
                ON DUPLICATE KEY UPDATE read_at = read_at
                """, userId, releaseId);
    }

    public Map<String, Object> releases(int page, int size) {
        int safePage = Math.max(page, 1);
        int safeSize = Math.min(Math.max(size, 1), 100);
        List<Map<String, Object>> records = jdbcTemplate.queryForList("""
                SELECT r.*, (SELECT COUNT(*) FROM user_content_release_read u WHERE u.release_id = r.id) read_count
                FROM copper_content_release r ORDER BY r.published_at DESC, r.id DESC LIMIT ? OFFSET ?
                """, safeSize, (safePage - 1) * safeSize);
        return page(records, safePage, safeSize, scalar("SELECT COUNT(*) FROM copper_content_release"));
    }

    @Scheduled(fixedDelay = 60_000L)
    @Transactional
    public void publishDueContent() {
        publishDueType("ACUPOINT");
        publishDueType("STORY");
    }

    private void publishDueType(String type) {
        String table = tableFor(type);
        List<Map<String, Object>> due = jdbcTemplate.queryForList(
                "SELECT * FROM " + table + " WHERE status = 'SCHEDULED' AND scheduled_at <= CURRENT_TIMESTAMP ORDER BY scheduled_at LIMIT 50");
        for (Map<String, Object> row : due) {
            Long operatorId = row.get("reviewed_by") == null ? null : number(row.get("reviewed_by")).longValue();
            try {
                publishRevision(type, row, operatorId);
            } catch (RuntimeException error) {
                jdbcTemplate.update("UPDATE " + table + " SET publish_error = ? WHERE id = ? AND status = 'SCHEDULED'",
                        error.getMessage(), row.get("id"));
            }
        }
    }

    private Long saveDraft(String table, String keyColumn, String key, Map<String, Object> payload,
                           String storyTitle, String storySummary, Long operatorId) {
        List<Map<String, Object>> active = jdbcTemplate.queryForList(
                "SELECT * FROM " + table + " WHERE " + keyColumn + " = ? AND status IN ('DRAFT','IN_REVIEW','APPROVED','SCHEDULED') ORDER BY version_no DESC LIMIT 1", key);
        String payloadJson = writeJson(payload);
        if (!active.isEmpty()) {
            Map<String, Object> row = active.get(0);
            if (!"DRAFT".equals(row.get("status"))) {
                throw new BusinessException("409", "当前内容已提交审核，不能继续修改");
            }
            long id = number(row.get("id")).longValue();
            if ("STORY".equals(tableType(table))) {
                jdbcTemplate.update("UPDATE " + table + " SET title = ?, summary = ?, cover_path = ?, payload_json = ?, updated_at = CURRENT_TIMESTAMP WHERE id = ?",
                        storyTitle, storySummary, payload.get("coverPath"), payloadJson, id);
            } else {
                jdbcTemplate.update("UPDATE " + table + " SET payload_json = ?, updated_at = CURRENT_TIMESTAMP WHERE id = ?", payloadJson, id);
            }
            return id;
        }
        Integer maxVersion = jdbcTemplate.queryForObject(
                "SELECT COALESCE(MAX(version_no), 0) FROM " + table + " WHERE " + keyColumn + " = ?", Integer.class, key);
        int nextVersion = (maxVersion == null ? 0 : maxVersion) + 1;
        if ("STORY".equals(tableType(table))) {
            jdbcTemplate.update("INSERT INTO " + table + " (story_code, version_no, status, title, summary, cover_path, payload_json, created_by) VALUES (?, ?, 'DRAFT', ?, ?, ?, ?, ?)",
                    key, nextVersion, storyTitle, storySummary, payload.get("coverPath"), payloadJson, operatorId);
        } else {
            jdbcTemplate.update("INSERT INTO " + table + " (acupoint_code, version_no, status, payload_json, created_by) VALUES (?, ?, 'DRAFT', ?, ?)",
                    key, nextVersion, payloadJson, operatorId);
        }
        return jdbcTemplate.queryForObject("SELECT id FROM " + table + " WHERE " + keyColumn + " = ? AND version_no = ?",
                Long.class, key, nextVersion);
    }

    private void updateStatus(String table, long id, String actual, String expected, String target,
                              Long operatorId, LocalDateTime scheduledAt) {
        if (!expected.equals(actual)) throw new BusinessException("409", "内容状态已改变，请刷新后再操作");
        int updated;
        if ("APPROVED".equals(target)) {
            updated = jdbcTemplate.update("UPDATE " + table + " SET status = 'APPROVED', reviewed_by = ?, reviewed_at = CURRENT_TIMESTAMP WHERE id = ? AND status = 'IN_REVIEW'",
                    operatorId, id);
        } else if ("SCHEDULED".equals(target)) {
            updated = jdbcTemplate.update("UPDATE " + table + " SET status = 'SCHEDULED', scheduled_at = ?, publish_error = NULL WHERE id = ? AND status = 'APPROVED'",
                    Timestamp.valueOf(scheduledAt), id);
        } else {
            updated = jdbcTemplate.update("UPDATE " + table + " SET status = ? WHERE id = ? AND status = ?", target, id, expected);
        }
        if (updated != 1) throw new BusinessException("409", "内容状态已被其他管理员修改");
    }

    private void publishRevision(String type, Map<String, Object> row, Long operatorId) {
        String table = tableFor(type);
        String keyColumn = keyColumnFor(type);
        long id = number(row.get("id")).longValue();
        String key = string(row.get(keyColumn));
        int version = number(row.get("version_no")).intValue();
        Map<String, Object> payload = readJson(row.get("payload_json"));
        List<String> errors = "ACUPOINT".equals(type) ? validateAcupoint(payload) : validateStory(payload);
        if (!errors.isEmpty()) throw new BusinessException("400", String.join("；", errors));
        jdbcTemplate.update("UPDATE " + table + " SET status = 'ARCHIVED' WHERE " + keyColumn + " = ? AND status = 'PUBLISHED' AND id <> ?", key, id);
        int updated = jdbcTemplate.update("UPDATE " + table + " SET status = 'PUBLISHED', published_by = ?, published_at = CURRENT_TIMESTAMP, publish_error = NULL WHERE id = ? AND status IN ('APPROVED','SCHEDULED')",
                operatorId, id);
        if (updated == 0 && !"PUBLISHED".equals(row.get("status"))) {
            throw new BusinessException("409", "内容已发布或状态已变更");
        }
        String title;
        String summary;
        String route;
        if ("ACUPOINT".equals(type)) {
            writePublishedAcupoint(payload);
            title = "新的文化星：" + string(payload.get("name"));
            summary = string(payload.get("childDescription"));
            route = "/copper-man?acupoint=" + key;
        } else {
            title = string(payload.get("title"));
            summary = string(payload.get("summary"));
            route = "/doctor-story?story=" + key;
        }
        jdbcTemplate.update("""
                INSERT INTO copper_content_release
                  (content_type, content_key, version_no, title, summary, route, published_by)
                VALUES (?, ?, ?, ?, ?, ?, ?)
                ON DUPLICATE KEY UPDATE title = VALUES(title), summary = VALUES(summary), route = VALUES(route), retracted_at = NULL
                """, type, key, version, title, summary, route, operatorId);
    }

    private void archiveRevision(String type, Map<String, Object> row, Long operatorId) {
        String status = string(row.get("status"));
        if (!Set.of("PUBLISHED", "SCHEDULED", "APPROVED").contains(status)) {
            throw new BusinessException("409", "当前状态不能归档");
        }
        String table = tableFor(type);
        long id = number(row.get("id")).longValue();
        jdbcTemplate.update("UPDATE " + table + " SET status = 'ARCHIVED', scheduled_at = NULL WHERE id = ? AND status = ?", id, status);
        if ("ACUPOINT".equals(type) && "PUBLISHED".equals(status)) {
            jdbcTemplate.update("UPDATE acupoint_knowledge SET enabled = 0 WHERE code = ?", row.get("acupoint_code"));
        }
        if ("PUBLISHED".equals(status)) {
            jdbcTemplate.update("UPDATE copper_content_release SET retracted_at = CURRENT_TIMESTAMP WHERE content_type = ? AND content_key = ? AND version_no = ?",
                    type, row.get(keyColumnFor(type)), row.get("version_no"));
        }
    }

    private void writePublishedAcupoint(Map<String, Object> p) {
        jdbcTemplate.update("""
                INSERT INTO acupoint_knowledge
                  (code, point_number, name, pinyin, meridian_code, meridian_name, meridian_english,
                   model_id, body_area, standard_location, child_location, child_description, child_traditional_use,
                   safety_tip, source_name, source_link, traditional_use_source_name, traditional_use_source_link,
                   position_x, position_y, position_z, sort_order, enabled)
                VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
                ON DUPLICATE KEY UPDATE point_number = VALUES(point_number), name = VALUES(name),
                  pinyin = VALUES(pinyin), meridian_code = VALUES(meridian_code), meridian_name = VALUES(meridian_name),
                  meridian_english = VALUES(meridian_english), model_id = VALUES(model_id), body_area = VALUES(body_area),
                  standard_location = VALUES(standard_location), child_location = VALUES(child_location),
                  child_description = VALUES(child_description), child_traditional_use = VALUES(child_traditional_use),
                  safety_tip = VALUES(safety_tip),
                  source_name = VALUES(source_name), source_link = VALUES(source_link),
                  traditional_use_source_name = VALUES(traditional_use_source_name),
                  traditional_use_source_link = VALUES(traditional_use_source_link),
                  position_x = VALUES(position_x), position_y = VALUES(position_y), position_z = VALUES(position_z),
                  sort_order = VALUES(sort_order), enabled = VALUES(enabled)
                """, p.get("code"), p.get("pointNumber"), p.get("name"), p.get("pinyin"),
                p.get("meridianCode"), p.get("meridianName"), p.get("meridianEnglish"), p.get("modelId"),
                p.get("bodyArea"), p.get("standardLocation"), p.get("childLocation"), p.get("childDescription"),
                p.get("childTraditionalUse"), p.get("safetyTip"), p.get("sourceName"), p.get("sourceLink"),
                p.get("traditionalUseSourceName"), p.get("traditionalUseSourceLink"), p.get("positionX"),
                p.get("positionY"), p.get("positionZ"), p.get("sortOrder"),
                !Boolean.FALSE.equals(p.get("enabled")));
    }

    private Map<String, Object> storyProgress(Long userId, String code) {
        List<Map<String, Object>> rows = jdbcTemplate.queryForList("""
                SELECT progress_json, completed, completed_at, updated_at
                FROM user_copper_story_progress WHERE user_id = ? AND story_code = ?
                """, userId, normalizeStoryCode(code));
        if (rows.isEmpty()) return Map.of("data", Map.of(), "completed", false);
        Map<String, Object> row = rows.get(0);
        return Map.of("data", readJson(row.get("progress_json")),
                "completed", truthy(row.get("completed")),
                "completedAt", Objects.toString(row.get("completed_at"), ""),
                "updatedAt", Objects.toString(row.get("updated_at"), ""));
    }

    private List<String> validateAcupoint(Map<String, Object> p) {
        List<String> errors = new ArrayList<>();
        required(errors, p, "code", "穴位编号");
        required(errors, p, "name", "穴位名称");
        required(errors, p, "meridianCode", "经络编号");
        required(errors, p, "meridianName", "经络名称");
        required(errors, p, "bodyArea", "身体区域");
        required(errors, p, "childLocation", "儿童观察提示");
        required(errors, p, "childDescription", "儿童文化档案");
        required(errors, p, "sourceName", "内容来源");
        if (blank(p.get("safetyTip"))) p.put("safetyTip", CHILD_SAFETY_TIP);
        length(errors, p, "childLocation", 120, "儿童观察提示");
        length(errors, p, "childDescription", 160, "儿童文化档案");
        boolean modeled = p.get("positionX") != null && p.get("positionY") != null && p.get("positionZ") != null;
        if (modeled) {
            required(errors, p, "childTraditionalUse", "儿童传统用途");
            required(errors, p, "traditionalUseSourceName", "传统用途来源");
        }
        length(errors, p, "childTraditionalUse", 180, "儿童传统用途");
        String childCopy = string(p.get("childLocation")) + string(p.get("childDescription"))
                + string(p.get("childTraditionalUse"));
        if (childCopy.matches(".*(主治|治疗|治愈|疗效|保证|特别管用|按一按|按揉|按摩|手法|针刺|扎针|刺激穴位|自行操作|自己取穴).*")) {
            errors.add("儿童文案不得包含治疗承诺或身体操作指导");
        }
        return errors;
    }

    private List<String> validateStory(Map<String, Object> p) {
        List<String> errors = new ArrayList<>();
        required(errors, p, "storyCode", "故事编号");
        required(errors, p, "title", "故事名称");
        required(errors, p, "summary", "故事简介");
        listAtLeast(errors, p, "pages", 3, "故事场景");
        listAtLeast(errors, p, "clues", 2, "故事线索");
        mapRequired(errors, p, "reasoning", "推理问题");
        mapRequired(errors, p, "safety", "安全回顾");
        listAtLeast(errors, p, "rewards", 1, "故事奖励");
        String all = writeJson(p);
        if (all.matches(".*(治疗|疗效|保证治好|按揉穴位|自己扎针|给同学针灸).*")) {
            errors.add("故事不得包含治疗承诺或模仿操作指导");
        }
        return errors;
    }

    private Map<String, Object> latestRevision(String table, String keyColumn, String key) {
        List<Map<String, Object>> rows = jdbcTemplate.queryForList(
                "SELECT * FROM " + table + " WHERE " + keyColumn + " = ? ORDER BY version_no DESC LIMIT 1", key);
        if (rows.isEmpty()) throw new BusinessException("404", "内容不存在");
        return rows.get(0);
    }

    private Map<String, Object> page(List<Map<String, Object>> records, int current, int size, Integer total) {
        return Map.of("records", records, "current", current, "size", size, "total", total == null ? 0 : total);
    }

    private int scalar(String sql) {
        Integer value = jdbcTemplate.queryForObject(sql, Integer.class);
        return value == null ? 0 : value;
    }

    private void audit(Long actor, String action, String type, String key, Object details) {
        auditEventService.record(actor, null, action, type, key, "SUCCESS", details);
    }

    private String writeJson(Object value) {
        try { return objectMapper.writeValueAsString(value); }
        catch (Exception e) { throw new BusinessException("400", "内容数据无法序列化", e); }
    }

    private Map<String, Object> readJson(Object value) {
        try {
            if (value == null) return new LinkedHashMap<>();
            return objectMapper.readValue(value.toString(), new TypeReference<>() {});
        } catch (Exception e) {
            throw new BusinessException("500", "内容数据已损坏", e);
        }
    }

    private void required(List<String> errors, Map<String, Object> p, String field, String label) {
        if (blank(p.get(field))) errors.add(label + "不能为空");
    }

    private void length(List<String> errors, Map<String, Object> p, String field, int max, String label) {
        if (string(p.get(field)).length() > max) errors.add(label + "不能超过" + max + "个字");
    }

    private void listAtLeast(List<String> errors, Map<String, Object> p, String field, int min, String label) {
        if (!(p.get(field) instanceof List<?> list) || list.size() < min) errors.add(label + "至少需要" + min + "项");
    }

    private void mapRequired(List<String> errors, Map<String, Object> p, String field, String label) {
        if (!(p.get(field) instanceof Map<?, ?> map) || map.isEmpty()) errors.add(label + "不能为空");
    }

    private String normalizeCode(String code) {
        if (code == null || !code.trim().toUpperCase(Locale.ROOT).matches("[A-Z]{1,3}-[0-9]{1,3}")) {
            throw new BusinessException("400", "穴位编号格式应为 LU-1");
        }
        return code.trim().toUpperCase(Locale.ROOT);
    }

    private String normalizeStoryCode(String code) {
        if (code == null || !code.trim().toLowerCase(Locale.ROOT).matches("[a-z0-9][a-z0-9-]{2,63}")) {
            throw new BusinessException("400", "故事编号只能使用小写英文、数字和连字符");
        }
        return code.trim().toLowerCase(Locale.ROOT);
    }

    private String normalizeType(String type) {
        String normalized = type == null ? "" : type.trim().toUpperCase(Locale.ROOT);
        if (!TRANSITION_TYPES.contains(normalized)) throw new BusinessException("400", "未知的内容类型");
        return normalized;
    }

    private String normalizeOptionalStatus(String status) {
        if (status == null || status.isBlank() || "ALL".equalsIgnoreCase(status)) return null;
        String normalized = status.trim().toUpperCase(Locale.ROOT);
        if (!Set.of("DRAFT", "IN_REVIEW", "APPROVED", "SCHEDULED", "PUBLISHED", "ARCHIVED").contains(normalized)) {
            throw new BusinessException("400", "未知的内容状态");
        }
        return normalized;
    }

    private String tableFor(String type) { return "ACUPOINT".equals(type) ? "copper_acupoint_revision" : "copper_story_revision"; }
    private String keyColumnFor(String type) { return "ACUPOINT".equals(type) ? "acupoint_code" : "story_code"; }
    private String tableType(String table) { return table.contains("story") ? "STORY" : "ACUPOINT"; }
    private String string(Object value) { return value == null ? "" : value.toString().trim(); }
    private boolean blank(Object value) { return string(value).isBlank(); }
    private Number number(Object value) { return value instanceof Number n ? n : Long.parseLong(value.toString()); }
    private boolean truthy(Object value) { return value instanceof Boolean b ? b : value instanceof Number n && n.intValue() != 0; }
}
