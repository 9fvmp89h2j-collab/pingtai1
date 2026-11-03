package org.example.springboot.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.Resource;
import org.example.springboot.dto.command.SafetyGameConfigDraftCommandDTO;
import org.example.springboot.exception.BusinessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Service
public class SafetyGameConfigService {
    private static final List<String> LEVELS = List.of("BEGINNER", "INTERMEDIATE", "ADVANCED");

    @Resource
    private JdbcTemplate jdbcTemplate;
    @Resource
    private ObjectMapper objectMapper;

    public Map<String, Object> getPublicConfig() {
        Revision revision = loadLatest("PUBLISHED");
        Map<String, Object> config = deepCopy(revision.config);
        sanitizePublicConfig(config);
        return envelope(revision, config);
    }

    public Map<String, Object> getAdminConfig() {
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("published", optionalLatest("PUBLISHED"));
        result.put("draft", optionalLatest("DRAFT"));
        return result;
    }

    @Transactional
    public Map<String, Object> saveDraft(SafetyGameConfigDraftCommandDTO command, Long operatorId) {
        if (command == null || command.getConfig() == null) {
            throw new BusinessException("安全守护案配置不能为空");
        }
        Revision currentDraft = findLatest("DRAFT");
        String payload = write(command.getConfig());
        if (currentDraft == null) {
            Revision published = loadLatest("PUBLISHED");
            int nextVersion = published.version + 1;
            jdbcTemplate.update("""
                    INSERT INTO safety_game_config_revision
                    (version_no, base_version, edit_version, status, payload_json, created_by, updated_by)
                    VALUES (?, ?, 1, 'DRAFT', ?, ?, ?)
                    """, nextVersion, published.version, payload, operatorId, operatorId);
            Long id = jdbcTemplate.queryForObject("SELECT LAST_INSERT_ID()", Long.class);
            return envelope(loadById(id), null);
        }
        if (!currentDraft.id.equals(command.getRevisionId())
                || command.getEditVersion() == null
                || currentDraft.editVersion != command.getEditVersion()) {
            throw new BusinessException("配置已被其他管理员更新，请刷新后重试");
        }
        jdbcTemplate.update("""
                UPDATE safety_game_config_revision
                SET payload_json = ?, edit_version = edit_version + 1, updated_by = ?
                WHERE id = ? AND status = 'DRAFT' AND edit_version = ?
                """, payload, operatorId, currentDraft.id, currentDraft.editVersion);
        return envelope(loadById(currentDraft.id), null);
    }

    public Map<String, Object> validate(Map<String, Object> config) {
        List<String> errors = validateConfig(config);
        return Map.of("valid", errors.isEmpty(), "errors", errors);
    }

    @Transactional
    public Map<String, Object> publish(Long revisionId, Integer editVersion, Long operatorId) {
        Revision draft = loadById(revisionId);
        if (!"DRAFT".equals(draft.status)) {
            throw new BusinessException("只有草稿版本可以发布");
        }
        if (editVersion == null || editVersion != draft.editVersion) {
            throw new BusinessException("草稿已发生变化，请刷新后重试");
        }
        List<String> errors = validateConfig(draft.config);
        if (!errors.isEmpty()) {
            throw new BusinessException("发布校验失败：" + String.join("；", errors));
        }
        jdbcTemplate.update("UPDATE safety_game_config_revision SET status = 'ARCHIVED' WHERE status = 'PUBLISHED'");
        jdbcTemplate.update("""
                UPDATE safety_game_config_revision
                SET status = 'PUBLISHED', published_at = CURRENT_TIMESTAMP, updated_by = ?
                WHERE id = ? AND status = 'DRAFT'
                """, operatorId, revisionId);
        return getPublicConfig();
    }

    public Map<String, Object> checkScenarioAnswer(Long revisionId, String scenarioId, String optionId) {
        Revision revision = loadLatest("PUBLISHED");
        if (!revision.id.equals(revisionId)) {
            throw new BusinessException("安全守护案内容已更新，请重新开始第二关");
        }
        for (Map<String, Object> scenario : maps(revision.config.get("scenarios"))) {
            if (Boolean.FALSE.equals(scenario.get("enabled"))) continue;
            if (scenarioId.equals(text(scenario.get("id")))) {
                String correctId = text(scenario.get("correctOptionId"));
                boolean optionExists = maps(scenario.get("options")).stream()
                        .anyMatch(option -> optionId.equals(text(option.get("id"))));
                if (!optionExists) throw new BusinessException("情境选项不存在");
                Map<String, Object> response = new LinkedHashMap<>();
                response.put("correct", optionId.equals(correctId));
                response.put("correctOptionId", correctId);
                response.put("explanation", text(scenario.get("explanation")));
                return response;
            }
        }
        throw new BusinessException("情境不存在或已停用");
    }

    public Map<String, Object> memoryProgress(Long userId) {
        List<Map<String, Object>> rows = jdbcTemplate.query("""
                SELECT max_unlocked_level, max_completed_level FROM user_safety_memory_progress WHERE user_id = ?
                """, (rs, rowNum) -> {
                    Map<String, Object> value = new LinkedHashMap<>();
                    value.put("maxUnlockedLevel", rs.getString("max_unlocked_level"));
                    value.put("maxCompletedLevel", rs.getString("max_completed_level"));
                    return value;
                }, userId);
        if (!rows.isEmpty()) return rows.get(0);
        Map<String, Object> initial = new LinkedHashMap<>();
        initial.put("maxUnlockedLevel", "BEGINNER");
        initial.put("maxCompletedLevel", null);
        return initial;
    }

    @Transactional
    public Map<String, Object> completeMemoryLevel(Long userId, String completedLevel) {
        String normalized = text(completedLevel).toUpperCase();
        int completedRank = LEVELS.indexOf(normalized);
        if (completedRank < 0) throw new BusinessException("翻牌难度无效");
        String current = text(memoryProgress(userId).get("maxUnlockedLevel"));
        int currentRank = Math.max(0, LEVELS.indexOf(current));
        if (completedRank > currentRank) throw new BusinessException("请先完成上一难度");
        String unlocked = LEVELS.get(Math.min(LEVELS.size() - 1, Math.max(currentRank, completedRank + 1)));
        String previousCompleted = text(memoryProgress(userId).get("maxCompletedLevel"));
        int previousCompletedRank = LEVELS.indexOf(previousCompleted);
        String maxCompleted = LEVELS.get(Math.max(previousCompletedRank, completedRank));
        jdbcTemplate.update("""
                INSERT INTO user_safety_memory_progress (user_id, max_unlocked_level, max_completed_level)
                VALUES (?, ?, ?)
                ON DUPLICATE KEY UPDATE max_unlocked_level = VALUES(max_unlocked_level),
                    max_completed_level = VALUES(max_completed_level)
                """, userId, unlocked, maxCompleted);
        return Map.of("maxUnlockedLevel", unlocked, "maxCompletedLevel", maxCompleted, "completedLevel", normalized);
    }

    public List<String> validateConfig(Map<String, Object> config) {
        List<String> errors = new ArrayList<>();
        if (config == null) return List.of("配置不能为空");
        List<Map<String, Object>> scenarios = maps(config.get("scenarios")).stream()
                .filter(item -> !Boolean.FALSE.equals(item.get("enabled"))).toList();
        if (scenarios.size() < 3 || scenarios.size() > 12) errors.add("启用的情境题需为3至12个");
        Set<String> scenarioIds = new LinkedHashSet<>();
        for (Map<String, Object> scenario : scenarios) {
            String id = text(scenario.get("id"));
            if (id.isBlank() || !scenarioIds.add(id)) errors.add("情境编号不能为空或重复");
            if (text(scenario.get("title")).isBlank() || text(scenario.get("prompt")).isBlank()) errors.add("情境标题和题干不能为空");
            if (!hasVisual(scenario) || text(scenario.get("imageAlt")).isBlank()) errors.add("情境“" + id + "”缺少图片或图片说明");
            List<Map<String, Object>> options = maps(scenario.get("options"));
            if (options.size() != 3) errors.add("情境“" + id + "”必须配置3个选项");
            Set<String> optionIds = new LinkedHashSet<>();
            for (Map<String, Object> option : options) {
                String optionId = text(option.get("id"));
                if (optionId.isBlank() || !optionIds.add(optionId)) errors.add("情境“" + id + "”选项编号不能为空或重复");
                if (text(option.get("text")).isBlank() || !hasVisual(option) || text(option.get("imageAlt")).isBlank()) {
                    errors.add("情境“" + id + "”的选项缺少文字、图案或图片说明");
                }
            }
            if (!optionIds.contains(text(scenario.get("correctOptionId")))) errors.add("情境“" + id + "”未设置有效正确答案");
            if (text(scenario.get("explanation")).isBlank()) errors.add("情境“" + id + "”缺少解析");
        }

        Map<String, Object> memory = map(config.get("memory"));
        List<Map<String, Object>> pairs = maps(memory.get("pairs")).stream()
                .filter(item -> !Boolean.FALSE.equals(item.get("enabled"))).toList();
        if (pairs.size() < 8) errors.add("翻牌至少需要8组启用配对");
        Map<String, Map<String, Object>> pairById = new LinkedHashMap<>();
        for (Map<String, Object> pair : pairs) {
            String id = text(pair.get("id"));
            if (id.isBlank() || pairById.put(id, pair) != null) errors.add("翻牌配对编号不能为空或重复");
            if (text(pair.get("name")).isBlank() || text(pair.get("explanation")).isBlank()) errors.add("翻牌配对名称和解释不能为空");
            for (String side : List.of("left", "right")) {
                Map<String, Object> visual = map(pair.get(side));
                if (!hasVisual(visual) || text(visual.get("imageAlt")).isBlank()) errors.add("翻牌配对“" + id + "”两侧均需图案和图片说明");
            }
        }
        Map<String, Object> levelMap = map(memory.get("levels"));
        validateLevel(errors, levelMap, pairById.keySet(), "BEGINNER", 2);
        validateLevel(errors, levelMap, pairById.keySet(), "INTERMEDIATE", 4);
        validateLevel(errors, levelMap, pairById.keySet(), "ADVANCED", 8);
        Map<String, Object> badge = map(memory.get("centerBadge"));
        if (!hasVisual(badge) || text(badge.get("imageAlt")).isBlank()) errors.add("中级中心守护徽章未配置");

        Map<String, Object> pledge = map(config.get("pledge"));
        List<Map<String, Object>> lines = maps(pledge.get("lines")).stream()
                .filter(item -> !Boolean.FALSE.equals(item.get("enabled"))).toList();
        if (lines.size() < 4 || lines.size() > 8) errors.add("宣誓内容需为4至8句");
        if (lines.stream().anyMatch(line -> text(line.get("title")).isBlank() || text(line.get("text")).isBlank())) errors.add("宣誓标题和内容不能为空");
        Map<String, Object> certificate = map(pledge.get("certificate"));
        for (String field : List.of("title", "honor", "issuer", "blessing")) {
            if (text(certificate.get(field)).isBlank()) errors.add("证书字段“" + field + "”不能为空");
        }
        return errors.stream().distinct().toList();
    }

    private void validateLevel(List<String> errors, Map<String, Object> levels, Set<String> available, String level, int count) {
        List<String> ids = strings(levels.get(level));
        if (ids.size() != count || new LinkedHashSet<>(ids).size() != count || !available.containsAll(ids)) {
            errors.add(level + "难度必须配置" + count + "个不重复且已启用的配对");
        }
    }

    private void sanitizePublicConfig(Map<String, Object> config) {
        List<Map<String, Object>> scenarios = maps(config.get("scenarios")).stream()
                .filter(item -> !Boolean.FALSE.equals(item.get("enabled")))
                .map(this::deepCopy).toList();
        for (Map<String, Object> scenario : scenarios) {
            scenario.remove("correctOptionId");
            scenario.remove("explanation");
            List<Map<String, Object>> options = new ArrayList<>(maps(scenario.get("options")));
            Collections.shuffle(options);
            scenario.put("options", options);
        }
        config.put("scenarios", scenarios);
        Map<String, Object> memory = map(config.get("memory"));
        memory.put("pairs", maps(memory.get("pairs")).stream().filter(item -> !Boolean.FALSE.equals(item.get("enabled"))).toList());
        Map<String, Object> pledge = map(config.get("pledge"));
        pledge.put("lines", maps(pledge.get("lines")).stream().filter(item -> !Boolean.FALSE.equals(item.get("enabled"))).toList());
    }

    private Map<String, Object> optionalLatest(String status) {
        Revision revision = findLatest(status);
        return revision == null ? null : envelope(revision, null);
    }

    private Revision loadLatest(String status) {
        Revision revision = findLatest(status);
        if (revision == null) throw new BusinessException("安全守护案尚未发布配置");
        return revision;
    }

    private Revision findLatest(String status) {
        List<Revision> rows = jdbcTemplate.query("""
                SELECT id, version_no, base_version, edit_version, status, payload_json
                FROM safety_game_config_revision WHERE status = ? ORDER BY version_no DESC LIMIT 1
                """, (rs, rowNum) -> revision(rs.getLong("id"), rs.getInt("version_no"), rs.getInt("base_version"),
                rs.getInt("edit_version"), rs.getString("status"), rs.getString("payload_json")), status);
        return rows.isEmpty() ? null : rows.get(0);
    }

    private Revision loadById(Long id) {
        if (id == null) throw new BusinessException("配置修订号不能为空");
        List<Revision> rows = jdbcTemplate.query("""
                SELECT id, version_no, base_version, edit_version, status, payload_json
                FROM safety_game_config_revision WHERE id = ?
                """, (rs, rowNum) -> revision(rs.getLong("id"), rs.getInt("version_no"), rs.getInt("base_version"),
                rs.getInt("edit_version"), rs.getString("status"), rs.getString("payload_json")), id);
        if (rows.isEmpty()) throw new BusinessException("安全守护案配置不存在");
        return rows.get(0);
    }

    private Revision revision(long id, int version, int baseVersion, int editVersion, String status, String payload) {
        try {
            Map<String, Object> config = objectMapper.readValue(payload, new TypeReference<>() {});
            return new Revision(id, version, baseVersion, editVersion, status, config);
        } catch (JsonProcessingException error) {
            throw new BusinessException("安全守护案配置格式错误");
        }
    }

    private Map<String, Object> envelope(Revision revision, Map<String, Object> overrideConfig) {
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("revisionId", revision.id);
        result.put("version", revision.version);
        result.put("baseVersion", revision.baseVersion);
        result.put("editVersion", revision.editVersion);
        result.put("status", revision.status);
        result.put("config", overrideConfig == null ? revision.config : overrideConfig);
        return result;
    }

    private String write(Map<String, Object> config) {
        try {
            return objectMapper.writeValueAsString(config);
        } catch (JsonProcessingException error) {
            throw new BusinessException("安全守护案配置无法保存");
        }
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> deepCopy(Map<String, Object> source) {
        return objectMapper.convertValue(source, new TypeReference<>() {});
    }

    private static boolean hasVisual(Map<String, Object> value) {
        return !text(value.get("imagePath")).isBlank() || !text(value.get("visualKey")).isBlank();
    }

    @SuppressWarnings("unchecked")
    private static Map<String, Object> map(Object value) {
        return value instanceof Map<?, ?> m ? (Map<String, Object>) m : new LinkedHashMap<>();
    }

    @SuppressWarnings("unchecked")
    private static List<Map<String, Object>> maps(Object value) {
        if (!(value instanceof List<?> list)) return new ArrayList<>();
        return list.stream().filter(Map.class::isInstance).map(item -> (Map<String, Object>) item).toList();
    }

    private static List<String> strings(Object value) {
        if (!(value instanceof List<?> list)) return new ArrayList<>();
        return list.stream().map(SafetyGameConfigService::text).filter(item -> !item.isBlank()).toList();
    }

    private static String text(Object value) {
        return value == null ? "" : String.valueOf(value).trim();
    }

    private record Revision(Long id, int version, int baseVersion, int editVersion, String status,
                            Map<String, Object> config) {}
}
