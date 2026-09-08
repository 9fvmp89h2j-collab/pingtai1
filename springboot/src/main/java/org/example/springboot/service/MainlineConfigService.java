package org.example.springboot.service;

import jakarta.annotation.Resource;
import org.example.springboot.dto.command.MainlineConfigDraftCommandDTO;
import org.example.springboot.dto.response.MainlineAdminConfigResponseDTO;
import org.example.springboot.dto.response.MainlineConfigResponseDTO;
import org.example.springboot.dto.response.MainlineLevelConfigDTO;
import org.example.springboot.dto.response.MainlineRevisionSummaryDTO;
import org.example.springboot.dto.response.MainlineRewardConfigDTO;
import org.example.springboot.dto.response.MainlineTaskConfigDTO;
import org.example.springboot.dto.response.MainlineValidationResponseDTO;
import org.example.springboot.exception.BusinessException;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

/**
 * Mainline content and rule authority. The static snapshot is deliberately kept
 * as a compatibility baseline for old test databases and an unavailable config
 * store; production databases are initialized by V3__mainline_config_revision.
 */
@Service
public class MainlineConfigService {
    public static final String GAME_CODE = "xinglin-mainline";
    private static final int FIRST_VERSION = 1;
    private static final int MAX_REWARD_AMOUNT = 999;
    private static final Set<String> MATERIALS = Set.of(
            "bamboo-slip-shard", "apricot-kernel", "herbal-leaf", "meridian-star-sand",
            "acupoint-star-pearl", "copper-token", "mugwort-floss", "safety-bell", "star-compass"
    );
    private static final Set<String> LEVEL_IDS = Set.of(
            "checkin", "safe-start", "bamboo", "body", "meridian", "archive", "secret-room", "agency"
    );
    private static final Map<String, Integer> LEVEL_ORDERS = Map.of(
            "checkin", 1, "safe-start", 2, "bamboo", 3, "body", 4,
            "meridian", 5, "archive", 6, "secret-room", 7, "agency", 8);
    private static final Map<String, String> LEVEL_MAP_KEYS = Map.of(
            "checkin", "checkin", "safe-start", "safe-start", "bamboo", "bamboo", "body", "body",
            "meridian", "meridian", "archive", "archive", "secret-room", "secret-room", "agency", "agency");
    private static final Map<String, String> TASK_ROUTES = Map.of(
            "map.checkin", "/checkin", "main-safety-case", "/safety", "main-mist-in-xinglin", "/doctor-story",
            "main-hand-star-map", "/body-map", "meridian-river-completion", "/jingluo",
            "copper-man-daily-case", "/copper-man", "review-daily", "/review", "main-repair-agency", "/agency");
    private static final Map<String, String> TASK_TYPES = Map.of(
            "map.checkin", "checkin", "main-safety-case", "safety", "main-mist-in-xinglin", "story",
            "main-hand-star-map", "acupoint", "meridian-river-completion", "meridian",
            "copper-man-daily-case", "copper-man", "review-daily", "review", "main-repair-agency", "agency");
    private static final Set<String> ICON_OPTIONS = Set.of("1", "2", "3", "4", "5", "6", "7", "8");
    private static final Set<String> SEAL_OPTIONS = Set.of("今", "锁", "案", "星", "修", "阅", "行", "启");

    @Resource
    private JdbcTemplate jdbcTemplate;

    private volatile Boolean storageAvailable;

    public MainlineConfigResponseDTO getPublicConfig() {
        return toResponse(loadPublishedSnapshot());
    }

    public MainlineAdminConfigResponseDTO getAdminConfig() {
        MainlineAdminConfigResponseDTO response = new MainlineAdminConfigResponseDTO();
        Snapshot draft = findDraftSnapshot();
        response.setConfig(toResponse(draft == null ? loadPublishedSnapshot() : draft));
        response.setVersions(listVersions());
        return response;
    }

    public MainlineValidationResponseDTO validate(MainlineConfigDraftCommandDTO command) {
        Snapshot snapshot = snapshotFromCommand(command == null ? null : command.getLevels(),
                command == null ? null : command.getRevisionId(), null, "DRAFT", 0);
        MainlineValidationResponseDTO result = validateSnapshot(snapshot);
        result.setRevisionId(command == null ? null : command.getRevisionId());
        result.setVersion(snapshot.version);
        return result;
    }

    @Transactional
    public MainlineConfigResponseDTO saveDraft(MainlineConfigDraftCommandDTO command, Long operatorId) {
        requireStorage();
        if (command == null) {
            throw new BusinessException("400", "主线配置不能为空");
        }

        Snapshot submitted = snapshotFromCommand(command.getLevels(), command.getRevisionId(), operatorId,
                "DRAFT", command.getExpectedEditVersion() == null ? 0 : command.getExpectedEditVersion());
        MainlineValidationResponseDTO validation = validateSnapshot(submitted);
        if (!validation.isValid()) {
            throw new BusinessException("400", String.join("；", validation.getErrors()));
        }

        Long revisionId = command.getRevisionId();
        if (revisionId == null) {
            Snapshot base = findDraftSnapshot();
            if (base == null) base = loadPublishedSnapshot();
            int nextVersion = nextVersion();
            revisionId = insertRevision(nextVersion, base.version, operatorId, "DRAFT", 0);
            submitted.revisionId = revisionId;
            submitted.version = nextVersion;
            writeSnapshot(submitted);
        } else {
            Map<String, Object> current = getRevisionRow(revisionId);
            if (!"DRAFT".equals(current.get("status"))) {
                throw new BusinessException("409", "当前版本已经发布，请刷新后重新编辑");
            }
            int actualEditVersion = ((Number) current.get("edit_version")).intValue();
            if (command.getExpectedEditVersion() != null && actualEditVersion != command.getExpectedEditVersion()) {
                throw new BusinessException("409", "草稿已被其他管理员修改，请重新加载");
            }
            int version = ((Number) current.get("version_no")).intValue();
            int updated = jdbcTemplate.update("""
                    UPDATE game_config_revision
                    SET updated_by = ?, edit_version = edit_version + 1, updated_at = CURRENT_TIMESTAMP
                    WHERE id = ? AND status = 'DRAFT' AND edit_version = ?
                    """, operatorId, revisionId, actualEditVersion);
            if (updated != 1) {
                throw new BusinessException("409", "草稿已被其他管理员修改，请重新加载");
            }
            submitted.revisionId = revisionId;
            submitted.version = version;
            submitted.editVersion = actualEditVersion + 1;
            writeSnapshot(submitted);
        }
        return toResponse(loadSnapshotById(revisionId));
    }

    @Transactional
    public MainlineConfigResponseDTO publish(Long revisionId, Long operatorId) {
        requireStorage();
        if (revisionId == null) throw new BusinessException("400", "发布版本不能为空");
        Snapshot draft = loadSnapshotById(revisionId);
        if (!"DRAFT".equals(draft.status)) throw new BusinessException("409", "只有草稿版本可以发布");
        MainlineValidationResponseDTO validation = validateSnapshot(draft);
        if (!validation.isValid()) {
            throw new BusinessException("400", String.join("；", validation.getErrors()));
        }
        jdbcTemplate.update("""
                UPDATE game_config_revision
                SET status = 'ARCHIVED', updated_at = CURRENT_TIMESTAMP
                WHERE game_code = ? AND status = 'PUBLISHED'
                """, GAME_CODE);
        jdbcTemplate.update("""
                UPDATE game_config_revision
                SET status = 'PUBLISHED', published_by = ?, published_at = CURRENT_TIMESTAMP,
                    updated_by = ?, updated_at = CURRENT_TIMESTAMP
                WHERE id = ? AND status = 'DRAFT'
                """, operatorId, operatorId, revisionId);
        return toResponse(loadSnapshotById(revisionId));
    }

    @Transactional
    public MainlineConfigResponseDTO restore(int version, Long operatorId) {
        requireStorage();
        Snapshot source = loadSnapshotByVersion(version);
        if (source == null) throw new BusinessException("404", "找不到要恢复的主线版本");
        int nextVersion = nextVersion();
        Long revisionId = insertRevision(nextVersion, source.version, operatorId, "DRAFT", 0);
        source.revisionId = revisionId;
        source.version = nextVersion;
        source.status = "DRAFT";
        source.editVersion = 0;
        source.createdAt = null;
        source.updatedAt = null;
        source.publishedAt = null;
        writeSnapshot(source);
        return toResponse(loadSnapshotById(revisionId));
    }

    public List<MainlineRevisionSummaryDTO> listVersions() {
        if (!isStorageAvailable()) {
            MainlineRevisionSummaryDTO summary = new MainlineRevisionSummaryDTO();
            summary.setVersion(FIRST_VERSION);
            summary.setStatus("PUBLISHED");
            return List.of(summary);
        }
        return jdbcTemplate.query("""
                SELECT id, version_no, status, created_by, published_by, created_at, updated_at, published_at
                FROM game_config_revision WHERE game_code = ? ORDER BY version_no DESC
                """, (rs, rowNum) -> {
            MainlineRevisionSummaryDTO item = new MainlineRevisionSummaryDTO();
            item.setRevisionId(rs.getLong("id"));
            item.setVersion(rs.getInt("version_no"));
            item.setStatus(rs.getString("status"));
            item.setCreatedBy((Long) rs.getObject("created_by"));
            item.setPublishedBy((Long) rs.getObject("published_by"));
            item.setCreatedAt(toLocalDateTime(rs.getTimestamp("created_at")));
            item.setUpdatedAt(toLocalDateTime(rs.getTimestamp("updated_at")));
            item.setPublishedAt(toLocalDateTime(rs.getTimestamp("published_at")));
            return item;
        }, GAME_CODE);
    }

    public int publishedVersion() {
        return loadPublishedSnapshot().version;
    }

    public List<MainlineLevelConfigDTO> runtimeLevels() {
        return loadPublishedSnapshot().levels.stream().map(this::copyLevel).toList();
    }

    public int taskTarget(String taskCode, Integer version) {
        MainlineTaskConfigDTO task = taskConfig(taskCode, version);
        if (task == null || task.getTarget() == null) throw new BusinessException("400", "未知游戏任务");
        return task.getTarget();
    }

    public List<MainlineRewardConfigDTO> taskRewards(String taskCode, Integer version) {
        MainlineTaskConfigDTO task = taskConfig(taskCode, version);
        return task == null ? List.of() : task.getRewards().stream().map(this::copyReward).toList();
    }

    public MainlineTaskConfigDTO taskConfig(String taskCode, Integer version) {
        if (taskCode == null) return null;
        Snapshot snapshot = version == null || version <= 0
                ? loadPublishedSnapshot() : loadSnapshotByVersionOrDefault(version);
        MainlineTaskConfigDTO task = snapshot.tasks.get(taskCode);
        return task == null ? null : copyTask(task);
    }

    public boolean isKnownTask(String taskCode) {
        return taskConfig(taskCode, publishedVersion()) != null;
    }

    public String levelIdForTask(String taskCode, Integer version) {
        Snapshot snapshot = version == null || version <= 0
                ? loadPublishedSnapshot() : loadSnapshotByVersionOrDefault(version);
        for (MainlineLevelConfigDTO level : snapshot.levels) {
            if (level.getRequiredTaskIds().contains(taskCode)) return level.getId();
        }
        if (taskCode != null && taskCode.startsWith("meridian-route-")) return "meridian";
        return null;
    }

    public boolean isMainlineTask(String taskCode) {
        return LEVEL_IDS.stream().anyMatch(levelId -> {
            MainlineLevelConfigDTO level = loadPublishedSnapshot().levels.stream()
                    .filter(item -> levelId.equals(item.getId())).findFirst().orElse(null);
            return level != null && level.getRequiredTaskIds().contains(taskCode);
        });
    }

    private Snapshot loadPublishedSnapshot() {
        if (!isStorageAvailable()) return defaultSnapshot();
        try {
            Map<String, Object> row = jdbcTemplate.queryForMap("""
                    SELECT id, version_no, edit_version, status, created_at, updated_at, published_at
                    FROM game_config_revision
                    WHERE game_code = ? AND status = 'PUBLISHED'
                    ORDER BY version_no DESC LIMIT 1
                    """, GAME_CODE);
            return loadSnapshotFromRow(row);
        } catch (DataAccessException error) {
            return defaultSnapshot();
        }
    }

    private Snapshot findDraftSnapshot() {
        if (!isStorageAvailable()) return null;
        try {
            Map<String, Object> row = jdbcTemplate.queryForMap("""
                    SELECT id, version_no, edit_version, status, created_at, updated_at, published_at
                    FROM game_config_revision
                    WHERE game_code = ? AND status = 'DRAFT'
                    ORDER BY version_no DESC LIMIT 1
                    """, GAME_CODE);
            return loadSnapshotFromRow(row);
        } catch (DataAccessException error) {
            return null;
        }
    }

    private Snapshot loadSnapshotByVersionOrDefault(int version) {
        Snapshot snapshot = loadSnapshotByVersion(version);
        return snapshot == null ? defaultSnapshot() : snapshot;
    }

    private Snapshot loadSnapshotByVersion(int version) {
        if (!isStorageAvailable()) return version == FIRST_VERSION ? defaultSnapshot() : null;
        try {
            Map<String, Object> row = jdbcTemplate.queryForMap("""
                    SELECT id, version_no, edit_version, status, created_at, updated_at, published_at
                    FROM game_config_revision WHERE game_code = ? AND version_no = ?
                    """, GAME_CODE, version);
            return loadSnapshotFromRow(row);
        } catch (DataAccessException error) {
            return null;
        }
    }

    private Snapshot loadSnapshotById(Long revisionId) {
        if (!isStorageAvailable()) return defaultSnapshot();
        try {
            return loadSnapshotFromRow(getRevisionRow(revisionId));
        } catch (DataAccessException error) {
            throw new BusinessException("404", "主线配置版本不存在");
        }
    }

    private Snapshot loadSnapshotFromRow(Map<String, Object> row) {
        Snapshot snapshot = new Snapshot();
        snapshot.revisionId = ((Number) row.get("id")).longValue();
        snapshot.version = ((Number) row.get("version_no")).intValue();
        snapshot.editVersion = ((Number) row.getOrDefault("edit_version", 0)).intValue();
        snapshot.status = String.valueOf(row.get("status"));
        snapshot.createdAt = asLocalDateTime(row.get("created_at"));
        snapshot.updatedAt = asLocalDateTime(row.get("updated_at"));
        snapshot.publishedAt = asLocalDateTime(row.get("published_at"));

        Map<String, MainlineLevelConfigDTO> levels = new LinkedHashMap<>();
        jdbcTemplate.query("""
                SELECT level_id, order_no, label, status_text, description, icon, seal,
                       position_x, position_y, route, map_key, cover_path, public_flag
                FROM game_level_revision WHERE revision_id = ? ORDER BY order_no
                """, rs -> {
            MainlineLevelConfigDTO level = new MainlineLevelConfigDTO();
            level.setId(rs.getString("level_id"));
            level.setOrder(rs.getInt("order_no"));
            level.setLabel(rs.getString("label"));
            level.setStatusText(rs.getString("status_text"));
            level.setDescription(rs.getString("description"));
            level.setIcon(rs.getString("icon"));
            level.setSeal(rs.getString("seal"));
            level.setX(rs.getBigDecimal("position_x"));
            level.setY(rs.getBigDecimal("position_y"));
            level.setRoute(rs.getString("route"));
            level.setMapKey(rs.getString("map_key"));
            level.setCoverPath(rs.getString("cover_path"));
            level.setPublicLevel(rs.getBoolean("public_flag"));
            levels.put(level.getId(), level);
        }, revisionId(snapshot));
        snapshot.levels.addAll(levels.values());

        jdbcTemplate.query("""
                SELECT task_code, level_id, name, description, task_type, route, target, editable_flag
                FROM game_task_revision WHERE revision_id = ? ORDER BY task_code
                """, rs -> {
            MainlineTaskConfigDTO task = new MainlineTaskConfigDTO();
            task.setTaskCode(rs.getString("task_code"));
            task.setLevelId(rs.getString("level_id"));
            task.setName(rs.getString("name"));
            task.setDescription(rs.getString("description"));
            task.setTaskType(rs.getString("task_type"));
            task.setRoute(rs.getString("route"));
            task.setTarget(rs.getInt("target"));
            task.setEditable(rs.getBoolean("editable_flag"));
            snapshot.tasks.put(task.getTaskCode(), task);
            MainlineLevelConfigDTO level = levels.get(task.getLevelId());
            if (level != null && level.getRequiredTaskIds().stream().noneMatch(task.getTaskCode()::equals)) {
                level.getRequiredTaskIds().add(task.getTaskCode());
                level.getTasks().add(task);
            }
        }, revisionId(snapshot));

        jdbcTemplate.query("""
                SELECT task_code, sort_order, reward_type, item_code, amount, score_delta
                FROM game_task_reward_revision WHERE revision_id = ? ORDER BY task_code, sort_order
                """, rs -> {
            MainlineTaskConfigDTO task = snapshot.tasks.get(rs.getString("task_code"));
            if (task != null) {
                task.getRewards().add(new MainlineRewardConfigDTO(
                        rs.getString("reward_type"), rs.getString("item_code"),
                        rs.getInt("amount"), rs.getInt("score_delta")));
            }
        }, revisionId(snapshot));

        jdbcTemplate.query("""
                SELECT level_id, prerequisite_level_id
                FROM game_level_prerequisite_revision WHERE revision_id = ?
                ORDER BY level_id, prerequisite_level_id
                """, rs -> {
            MainlineLevelConfigDTO level = levels.get(rs.getString("level_id"));
            if (level != null) level.getPrerequisiteLevelIds().add(rs.getString("prerequisite_level_id"));
        }, revisionId(snapshot));

        if (snapshot.levels.isEmpty()) return defaultSnapshot();
        return snapshot;
    }

    private long revisionId(Snapshot snapshot) {
        return snapshot.revisionId == null ? 0L : snapshot.revisionId;
    }

    private Map<String, Object> getRevisionRow(Long revisionId) {
        return jdbcTemplate.queryForMap("""
                SELECT id, version_no, edit_version, status, created_at, updated_at, published_at
                FROM game_config_revision WHERE id = ? AND game_code = ?
                """, revisionId, GAME_CODE);
    }

    private Snapshot snapshotFromCommand(List<MainlineLevelConfigDTO> levels,
                                         Long revisionId,
                                         Long operatorId,
                                         String status,
                                         int editVersion) {
        Snapshot snapshot = new Snapshot();
        snapshot.revisionId = revisionId;
        snapshot.version = 0;
        snapshot.editVersion = editVersion;
        snapshot.status = status;
        if (levels != null) {
            for (MainlineLevelConfigDTO input : levels) {
                MainlineLevelConfigDTO level = copyLevel(input);
                snapshot.levels.add(level);
                for (MainlineTaskConfigDTO task : level.getTasks()) {
                    snapshot.tasks.put(task.getTaskCode(), copyTask(task));
                }
            }
        }
        Snapshot baseline = defaultSnapshot();
        baseline.tasks.forEach(snapshot.tasks::putIfAbsent);
        if (snapshot.version == 0) snapshot.version = baseline.version;
        return snapshot;
    }

    private MainlineValidationResponseDTO validateSnapshot(Snapshot snapshot) {
        MainlineValidationResponseDTO result = new MainlineValidationResponseDTO();
        List<String> errors = result.getErrors();
        if (snapshot == null || snapshot.levels == null) {
            errors.add("请提供主线关卡配置");
            result.setValid(false);
            return result;
        }
        if (snapshot.levels.size() != 8) errors.add("主线必须保留 8 个关卡");
        Set<String> ids = new LinkedHashSet<>();
        Set<Integer> orders = new HashSet<>();
        Map<String, MainlineLevelConfigDTO> byId = new HashMap<>();
        for (MainlineLevelConfigDTO level : snapshot.levels) {
            if (level == null) {
                errors.add("关卡配置不能为空");
                continue;
            }
            if (!ids.add(level.getId())) errors.add("存在重复关卡 ID：" + level.getId());
            if (level.getId() == null || !LEVEL_IDS.contains(level.getId())) errors.add("不支持的关卡 ID：" + level.getId());
            if (level.getOrder() == null || level.getOrder() < 1 || level.getOrder() > 8 || !orders.add(level.getOrder())) {
                errors.add("关卡顺序必须是 1 到 8 且不能重复");
            }
            if (level.getId() != null && !Objects.equals(LEVEL_ORDERS.get(level.getId()), level.getOrder())) {
                errors.add("关卡顺序不可修改：" + level.getId());
            }
            if (isBlank(level.getLabel()) || isBlank(level.getStatusText()) || isBlank(level.getDescription())) {
                errors.add("关卡标题、状态文案和说明不能为空：" + level.getId());
            }
            if (level.getId() != null && !Objects.equals(LEVEL_MAP_KEYS.get(level.getId()), level.getMapKey())) {
                errors.add("关卡 mapKey 不可修改：" + level.getId());
            }
            LevelSeed immutable = levelSeed(level.getId());
            if (!Boolean.TRUE.equals(level.getPublicLevel())) {
                errors.add("主线关卡必须保持公开：" + level.getId());
            }
            if (immutable != null && (!sameDecimal(level.getX(), immutable.x()) || !sameDecimal(level.getY(), immutable.y()))) {
                errors.add("关卡地图坐标不可修改：" + level.getId());
            }
            if (level.getIcon() == null || !ICON_OPTIONS.contains(level.getIcon())) {
                errors.add("关卡图标选项无效：" + level.getId());
            }
            if (level.getSeal() == null || !SEAL_OPTIONS.contains(level.getSeal())) {
                errors.add("关卡印章选项无效：" + level.getId());
            }
            if (level.getId() != null && level.getCoverPath() != null && !level.getCoverPath().isBlank()
                    && !level.getCoverPath().startsWith("/files/")) {
                errors.add("关卡封面路径必须来自文件服务：" + level.getId());
            }
            byId.put(level.getId(), level);
        }

        Map<String, String> immutableRoutes = Map.of(
                "checkin", "/checkin", "safe-start", "/safety", "bamboo", "/doctor-story",
                "body", "/body-map", "meridian", "/jingluo", "archive", "/copper-man",
                "secret-room", "/review", "agency", "/agency");
        Map<String, String> requiredTaskByLevel = Map.of(
                "checkin", "map.checkin", "safe-start", "main-safety-case", "bamboo", "main-mist-in-xinglin",
                "body", "main-hand-star-map", "meridian", "meridian-river-completion",
                "archive", "copper-man-daily-case", "secret-room", "review-daily", "agency", "main-repair-agency");
        for (String levelId : LEVEL_IDS) {
            MainlineLevelConfigDTO level = byId.get(levelId);
            if (level == null) continue;
            if (!Objects.equals(immutableRoutes.get(levelId), level.getRoute())) errors.add("关卡路由不可修改：" + levelId);
            if (level.getRequiredTaskIds().size() != 1 || !requiredTaskByLevel.get(levelId).equals(level.getRequiredTaskIds().get(0))) {
                errors.add("关卡任务绑定不可修改：" + levelId);
            }
            if (levelId.equals("checkin") && !level.getPrerequisiteLevelIds().isEmpty()) errors.add("首关不能设置前置关卡");
            if (!levelId.equals("checkin") && level.getPrerequisiteLevelIds().isEmpty()) errors.add("后续关卡必须设置前置关卡：" + levelId);
            for (String prerequisite : level.getPrerequisiteLevelIds()) {
                if (!byId.containsKey(prerequisite)) errors.add("前置关卡不存在：" + prerequisite);
                if (byId.containsKey(prerequisite) && byId.get(prerequisite).getOrder() >= level.getOrder()) {
                    errors.add("前置关卡必须排在当前关卡之前：" + levelId);
                }
            }
            if (level.getTasks().size() != 1) errors.add("每个主线关卡必须配置一个任务：" + levelId);
            for (MainlineTaskConfigDTO task : level.getTasks()) validateTask(task, levelId, errors);
        }
        if (hasCycle(byId)) errors.add("关卡前置关系存在循环");
        result.setValid(errors.isEmpty());
        return result;
    }

    private void validateTask(MainlineTaskConfigDTO task, String levelId, List<String> errors) {
        if (task == null || task.getTaskCode() == null) {
            errors.add("关卡任务不能为空：" + levelId);
            return;
        }
        if (!task.getTaskCode().equals(taskCodeForLevel(levelId))) errors.add("任务编码不可修改：" + levelId);
        if (!Objects.equals(levelId, task.getLevelId())) errors.add("任务绑定关卡不可修改：" + task.getTaskCode());
        if (!Objects.equals(TASK_ROUTES.get(task.getTaskCode()), task.getRoute())) errors.add("任务路由不可修改：" + task.getTaskCode());
        if (!Objects.equals(TASK_TYPES.get(task.getTaskCode()), task.getTaskType())) errors.add("玩法类型不可修改：" + task.getTaskCode());
        boolean expectedEditable = !"map.checkin".equals(task.getTaskCode());
        if (!Objects.equals(expectedEditable, task.getEditable())) errors.add("任务编辑权限不可修改：" + task.getTaskCode());
        if (isBlank(task.getName()) || isBlank(task.getDescription())) errors.add("任务名称和说明不能为空：" + task.getTaskCode());
        if (task.getTarget() == null || task.getTarget() < 1 || task.getTarget() > 999) errors.add("任务目标必须在 1 到 999 之间：" + task.getTaskCode());
        if ("meridian-river-completion".equals(task.getTaskCode()) && (task.getTarget() == null || task.getTarget() > 14)) {
            errors.add("经络星河总目标不能超过 14 条路线");
        }
        Set<String> rewards = new HashSet<>();
        for (MainlineRewardConfigDTO reward : task.getRewards()) {
            if (reward == null || reward.getRewardType() == null) {
                errors.add("奖励类型不能为空：" + task.getTaskCode());
                continue;
            }
            String key = reward.getRewardType() + ":" + reward.getItemCode();
            if (!rewards.add(key)) errors.add("奖励不能重复：" + task.getTaskCode());
            if ("MATERIAL".equals(reward.getRewardType())) {
                if (!MATERIALS.contains(reward.getItemCode())) errors.add("奖励材料未注册：" + reward.getItemCode());
                if (reward.getAmount() == null || reward.getAmount() < 1 || reward.getAmount() > MAX_REWARD_AMOUNT) errors.add("材料奖励数量无效：" + task.getTaskCode());
            } else if ("SCORE".equals(reward.getRewardType())) {
                if (reward.getScoreDelta() == null || reward.getScoreDelta() < 1 || reward.getScoreDelta() > MAX_REWARD_AMOUNT) errors.add("积分奖励数量无效：" + task.getTaskCode());
            } else {
                errors.add("不支持的奖励类型：" + reward.getRewardType());
            }
        }
    }

    private boolean hasCycle(Map<String, MainlineLevelConfigDTO> byId) {
        Set<String> visiting = new HashSet<>();
        Set<String> visited = new HashSet<>();
        for (String id : byId.keySet()) if (hasCycle(id, byId, visiting, visited)) return true;
        return false;
    }

    private boolean hasCycle(String id, Map<String, MainlineLevelConfigDTO> byId,
                             Set<String> visiting, Set<String> visited) {
        if (visited.contains(id)) return false;
        if (!visiting.add(id)) return true;
        MainlineLevelConfigDTO level = byId.get(id);
        if (level != null) for (String prerequisite : level.getPrerequisiteLevelIds()) {
            if (hasCycle(prerequisite, byId, visiting, visited)) return true;
        }
        visiting.remove(id);
        visited.add(id);
        return false;
    }

    private String taskCodeForLevel(String levelId) {
        return switch (levelId) {
            case "checkin" -> "map.checkin";
            case "safe-start" -> "main-safety-case";
            case "bamboo" -> "main-mist-in-xinglin";
            case "body" -> "main-hand-star-map";
            case "meridian" -> "meridian-river-completion";
            case "archive" -> "copper-man-daily-case";
            case "secret-room" -> "review-daily";
            case "agency" -> "main-repair-agency";
            default -> "";
        };
    }

    private void writeSnapshot(Snapshot snapshot) {
        jdbcTemplate.update("DELETE FROM game_level_revision WHERE revision_id = ?", snapshot.revisionId);
        jdbcTemplate.update("DELETE FROM game_task_reward_revision WHERE revision_id = ?", snapshot.revisionId);
        jdbcTemplate.update("DELETE FROM game_task_revision WHERE revision_id = ?", snapshot.revisionId);
        jdbcTemplate.update("DELETE FROM game_level_prerequisite_revision WHERE revision_id = ?", snapshot.revisionId);
        for (MainlineLevelConfigDTO level : snapshot.levels) {
            jdbcTemplate.update("""
                    INSERT INTO game_level_revision
                    (revision_id, level_id, order_no, label, status_text, description, icon, seal, position_x, position_y, route, map_key, cover_path, public_flag)
                    VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
                    """, snapshot.revisionId, level.getId(), level.getOrder(), level.getLabel(), level.getStatusText(),
                    level.getDescription(), level.getIcon(), level.getSeal(), level.getX(), level.getY(), level.getRoute(),
                    level.getMapKey(), level.getCoverPath(), Boolean.TRUE.equals(level.getPublicLevel()));
            for (MainlineTaskConfigDTO task : level.getTasks()) writeTask(snapshot.revisionId, task);
            for (String prerequisite : level.getPrerequisiteLevelIds()) {
                jdbcTemplate.update("""
                        INSERT INTO game_level_prerequisite_revision (revision_id, level_id, prerequisite_level_id)
                        VALUES (?, ?, ?)
                        """, snapshot.revisionId, level.getId(), prerequisite);
            }
        }
        snapshot.tasks.values().stream()
                .filter(task -> task.getLevelId() == null)
                .forEach(task -> writeTask(snapshot.revisionId, task));
    }

    private void writeTask(Long revisionId, MainlineTaskConfigDTO task) {
        jdbcTemplate.update("""
                INSERT INTO game_task_revision
                (revision_id, task_code, level_id, name, description, task_type, route, target, editable_flag)
                VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)
                """, revisionId, task.getTaskCode(), task.getLevelId(), task.getName(), task.getDescription(),
                task.getTaskType(), task.getRoute(), task.getTarget(), Boolean.TRUE.equals(task.getEditable()));
        int order = 0;
        for (MainlineRewardConfigDTO reward : task.getRewards()) {
            jdbcTemplate.update("""
                    INSERT INTO game_task_reward_revision
                    (revision_id, task_code, sort_order, reward_type, item_code, amount, score_delta)
                    VALUES (?, ?, ?, ?, ?, ?, ?)
                    """, revisionId, task.getTaskCode(), order++, reward.getRewardType(), reward.getItemCode(),
                    reward.getAmount() == null ? 0 : reward.getAmount(), reward.getScoreDelta() == null ? 0 : reward.getScoreDelta());
        }
    }

    private Long insertRevision(int version, int baseVersion, Long operatorId, String status, int editVersion) {
        jdbcTemplate.update("""
                INSERT INTO game_config_revision
                (game_code, version_no, status, base_version, edit_version, created_by, updated_by)
                VALUES (?, ?, ?, ?, ?, ?, ?)
                """, GAME_CODE, version, status, baseVersion, editVersion, operatorId, operatorId);
        return jdbcTemplate.queryForObject("SELECT LAST_INSERT_ID()", Long.class);
    }

    private int nextVersion() {
        Integer max = jdbcTemplate.queryForObject("SELECT COALESCE(MAX(version_no), 0) FROM game_config_revision WHERE game_code = ?", Integer.class, GAME_CODE);
        return (max == null ? 0 : max) + 1;
    }

    private void requireStorage() {
        if (!isStorageAvailable()) throw new BusinessException("500", "主线配置表未初始化，请先执行数据库迁移");
    }

    private boolean isStorageAvailable() {
        if (storageAvailable != null) return storageAvailable;
        try {
            jdbcTemplate.queryForObject("SELECT COUNT(*) FROM game_config_revision", Integer.class);
            storageAvailable = true;
        } catch (DataAccessException error) {
            storageAvailable = false;
        }
        return storageAvailable;
    }

    private MainlineConfigResponseDTO toResponse(Snapshot snapshot) {
        MainlineConfigResponseDTO response = new MainlineConfigResponseDTO();
        response.setRevisionId(snapshot.revisionId);
        response.setGameCode(GAME_CODE);
        response.setVersion(snapshot.version);
        response.setEditVersion(snapshot.editVersion);
        response.setStatus(snapshot.status);
        response.setUpdatedAt(snapshot.updatedAt);
        response.setPublishedAt(snapshot.publishedAt);
        response.setLevels(snapshot.levels.stream().sorted(Comparator.comparing(MainlineLevelConfigDTO::getOrder)).map(this::copyLevel).toList());
        return response;
    }

    private Snapshot defaultSnapshot() {
        Snapshot snapshot = new Snapshot();
        snapshot.revisionId = 1L;
        snapshot.version = FIRST_VERSION;
        snapshot.editVersion = 0;
        snapshot.status = "PUBLISHED";
        for (LevelSeed seed : LEVEL_SEEDS) {
            MainlineLevelConfigDTO level = new MainlineLevelConfigDTO();
            level.setId(seed.id());
            level.setOrder(seed.order());
            level.setLabel(seed.label());
            level.setStatusText(seed.statusText());
            level.setDescription(seed.description());
            level.setIcon(String.valueOf(seed.order()));
            level.setSeal(seed.order() == 1 ? "今" : "锁");
            level.setX(seed.x());
            level.setY(seed.y());
            level.setRoute(seed.route());
            level.setMapKey(seed.mapKey());
            level.setPublicLevel(true);
            level.setRequiredTaskIds(new ArrayList<>(List.of(seed.taskCode())));
            level.setPrerequisiteLevelIds(new ArrayList<>(seed.prerequisites()));
            MainlineTaskConfigDTO task = defaultTask(seed.taskCode(), seed.id());
            level.getTasks().add(task);
            snapshot.levels.add(level);
            snapshot.tasks.put(task.getTaskCode(), task);
        }
        List<MainlineTaskConfigDTO> auxiliary = List.of(
                defaultTask("daily-read-copper-story", null), defaultTask("daily-light-hand-stars", null),
                defaultTask("daily-safety-quiz", null), defaultTask("shunting-daily", null));
        auxiliary.forEach(task -> snapshot.tasks.put(task.getTaskCode(), task));
        return snapshot;
    }

    private MainlineTaskConfigDTO defaultTask(String taskCode, String levelId) {
        MainlineTaskConfigDTO task = new MainlineTaskConfigDTO();
        task.setTaskCode(taskCode);
        task.setLevelId(levelId);
        task.setEditable(levelId != null && !"checkin".equals(levelId));
        task.setRoute("/home-map");
        task.setTaskType("mainline");
        task.setTarget(1);
        switch (taskCode) {
            case "map.checkin" -> { task.setName("完成侦探社报到"); task.setDescription("完成侦探社报到并领取观察徽章。"); task.setTaskType("checkin"); task.setRoute("/checkin"); }
            case "main-safety-case" -> { task.setName("完成安全守护案"); task.setDescription("完成安全学习与安全宣誓，记住只观察、只学习、不自行针刺。"); task.setTaskType("safety"); task.setRoute("/safety"); task.setTarget(1); task.getRewards().add(material("safety-bell", 5)); task.getRewards().add(material("mugwort-floss", 3)); }
            case "main-mist-in-xinglin" -> { task.setName("杏林谷起雾"); task.setDescription("完成故事馆任务，找回第一束杏林谷星光。"); task.setTaskType("story"); task.setRoute("/doctor-story"); task.getRewards().add(material("bamboo-slip-shard", 3)); }
            case "main-hand-star-map" -> { task.setName("点亮身体地图"); task.setDescription("认识身体区域，找到穴位星点，并完成一次安全问答。"); task.setTaskType("acupoint"); task.setRoute("/body-map"); task.setTarget(10); task.getRewards().add(material("acupoint-star-pearl", 2)); task.getRewards().add(material("copper-token", 1)); task.getRewards().add(material("safety-bell", 1)); }
            case "meridian-river-completion" -> { task.setName("完成经络星河"); task.setDescription("完成经络路线并领取星河聚合奖励。"); task.setTaskType("meridian"); task.setRoute("/jingluo"); task.setTarget(14); task.getRewards().add(material("meridian-star-sand", 5)); task.getRewards().add(material("copper-token", 2)); }
            case "copper-man-daily-case" -> { task.setName("完成铜人观察案"); task.setDescription("完成首次铜人观察案件。"); task.setTaskType("copper-man"); task.setRoute("/copper-man"); task.getRewards().add(material("copper-token", 2)); task.getRewards().add(material("meridian-star-sand", 5)); }
            case "review-daily" -> { task.setName("完成星光修补"); task.setDescription("完成首次知识星点修补。"); task.setTaskType("review"); task.setRoute("/review"); task.getRewards().add(material("star-compass", 1)); }
            case "main-repair-agency" -> { task.setName("整理侦探社线索墙"); task.setDescription("收集线索并修复侦探社档案墙。"); task.setTaskType("agency"); task.setRoute("/agency"); }
            case "daily-read-copper-story" -> { task.setName("阅读一个针灸小故事"); task.setDescription("去故事馆完成《失踪竹简案》，回答故事后的安全小问题。"); task.setTaskType("story"); task.setRoute("/doctor-story"); task.getRewards().add(material("bamboo-slip-shard", 3)); task.getRewards().add(material("apricot-kernel", 2)); }
            case "daily-light-hand-stars" -> { task.setName("点亮 3 个穴位星点"); task.setDescription("跟着小铜人老师认识身体地图，找到 3 颗穴位星点。"); task.setTaskType("acupoint"); task.setRoute("/body-map"); task.setTarget(3); task.getRewards().add(material("acupoint-star-pearl", 3)); task.getRewards().add(material("copper-token", 1)); }
            case "daily-safety-quiz" -> { task.setName("完成 3 道安全判断题"); task.setDescription("听安全铃铛提醒，判断哪些行为可以做。"); task.setTaskType("safety"); task.setRoute("/safety"); task.setTarget(3); task.getRewards().add(material("safety-bell", 1)); task.getRewards().add(material("mugwort-floss", 1)); }
            case "shunting-daily" -> { task.setName("完成经络小火车"); task.setDescription("完成一次经络小火车观察挑战。"); task.setTaskType("shunting"); task.setRoute("/shunting-game"); }
            default -> { task.setName(taskCode); task.setDescription(taskCode); }
        }
        return task;
    }

    private MainlineRewardConfigDTO material(String code, int amount) {
        return new MainlineRewardConfigDTO("MATERIAL", code, amount, 0);
    }

    private MainlineLevelConfigDTO copyLevel(MainlineLevelConfigDTO source) {
        MainlineLevelConfigDTO target = new MainlineLevelConfigDTO();
        if (source == null) return target;
        target.setId(source.getId()); target.setOrder(source.getOrder()); target.setLabel(source.getLabel());
        target.setStatusText(source.getStatusText()); target.setDescription(source.getDescription());
        target.setIcon(source.getIcon()); target.setSeal(source.getSeal()); target.setX(source.getX()); target.setY(source.getY());
        target.setRoute(source.getRoute()); target.setMapKey(source.getMapKey()); target.setCoverPath(source.getCoverPath());
        target.setPublicLevel(source.getPublicLevel());
        target.setRequiredTaskIds(source.getRequiredTaskIds() == null
                ? new ArrayList<>() : new ArrayList<>(source.getRequiredTaskIds()));
        target.setPrerequisiteLevelIds(source.getPrerequisiteLevelIds() == null
                ? new ArrayList<>() : new ArrayList<>(source.getPrerequisiteLevelIds()));
        target.setTasks(source.getTasks() == null ? new ArrayList<>()
                : source.getTasks().stream().map(this::copyTask)
                .collect(java.util.stream.Collectors.toCollection(ArrayList::new)));
        return target;
    }

    private MainlineTaskConfigDTO copyTask(MainlineTaskConfigDTO source) {
        MainlineTaskConfigDTO target = new MainlineTaskConfigDTO();
        if (source == null) return target;
        target.setTaskCode(source.getTaskCode()); target.setLevelId(source.getLevelId()); target.setName(source.getName());
        target.setDescription(source.getDescription()); target.setTaskType(source.getTaskType()); target.setRoute(source.getRoute());
        target.setTarget(source.getTarget()); target.setEditable(source.getEditable());
        target.setRewards(source.getRewards() == null ? new ArrayList<>()
                : source.getRewards().stream().map(this::copyReward)
                .collect(java.util.stream.Collectors.toCollection(ArrayList::new)));
        return target;
    }

    private MainlineRewardConfigDTO copyReward(MainlineRewardConfigDTO source) {
        return source == null ? null
                : new MainlineRewardConfigDTO(source.getRewardType(), source.getItemCode(), source.getAmount(), source.getScoreDelta());
    }

    private boolean isBlank(String value) { return value == null || value.isBlank(); }

    private LevelSeed levelSeed(String levelId) {
        return LEVEL_SEEDS.stream().filter(seed -> seed.id().equals(levelId)).findFirst().orElse(null);
    }

    private boolean sameDecimal(BigDecimal actual, BigDecimal expected) {
        return actual != null && expected != null && actual.compareTo(expected) == 0;
    }

    private LocalDateTime toLocalDateTime(Timestamp timestamp) { return timestamp == null ? null : timestamp.toLocalDateTime(); }

    private LocalDateTime asLocalDateTime(Object value) {
        if (value instanceof Timestamp timestamp) return timestamp.toLocalDateTime();
        if (value instanceof LocalDateTime dateTime) return dateTime;
        return null;
    }

    private record LevelSeed(String id, int order, String label, String statusText, String description,
                             BigDecimal x, BigDecimal y, String route, String mapKey, String taskCode,
                             List<String> prerequisites) {}

    private static final List<LevelSeed> LEVEL_SEEDS = List.of(
            new LevelSeed("checkin", 1, "报到处", "入社报到", "完成侦探社报到，领取第一枚观察徽章。", bd(13.6), bd(9.5), "/checkin", "checkin", "map.checkin", List.of()),
            new LevelSeed("safe-start", 2, "安全守护案", "安全课堂", "先学会只观察、不针刺的安全规则，再开始后续调查。", bd(33.2), bd(11.8), "/safety", "safe-start", "main-safety-case", List.of("checkin")),
            new LevelSeed("bamboo", 3, "失踪竹简案", "故事馆任务", "阅读故事、搜集证据、完成推理和安全判断。", bd(39), bd(35), "/doctor-story", "bamboo", "main-mist-in-xinglin", List.of("safe-start")),
            new LevelSeed("body", 4, "身体地图追踪案", "身体地图", "完成区域学习和穴位归位，只做观察与文化知识学习。", bd(8.8), bd(40), "/body-map", "body", "main-hand-star-map", List.of("bamboo")),
            new LevelSeed("meridian", 5, "经络星河密令", "经络学习", "完成 14 条经络路线并领取星河聚合奖励。", bd(29), bd(60), "/jingluo", "meridian", "meridian-river-completion", List.of("body")),
            new LevelSeed("archive", 6, "铜人档案室", "小铜人馆", "完成首次铜人观察案件，后续案件作为每日重复内容。", bd(57.5), bd(47), "/copper-man", "archive", "copper-man-daily-case", List.of("meridian")),
            new LevelSeed("secret-room", 7, "星光修补册", "复习修补", "完成首次知识星点修补，后续错题作为复习任务。", bd(59.5), bd(70), "/review", "secret-room", "review-daily", List.of("archive")),
            new LevelSeed("agency", 8, "侦探社修复计划", "侦探社修复", "修复第一项门牌即完成主线，其余项目用于持续收集。", bd(14.8), bd(74), "/agency", "agency", "main-repair-agency", List.of("secret-room"))
    );

    private static BigDecimal bd(double value) { return BigDecimal.valueOf(value); }

    private static class Snapshot {
        private Long revisionId;
        private int version;
        private int editVersion;
        private String status;
        private LocalDateTime createdAt;
        private LocalDateTime updatedAt;
        private LocalDateTime publishedAt;
        private final List<MainlineLevelConfigDTO> levels = new ArrayList<>();
        private final Map<String, MainlineTaskConfigDTO> tasks = new LinkedHashMap<>();
    }
}
