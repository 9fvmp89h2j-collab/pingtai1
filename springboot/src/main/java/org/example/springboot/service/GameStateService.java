package org.example.springboot.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.Resource;
import org.example.springboot.dto.command.GameProgressEventCommandDTO;
import org.example.springboot.dto.command.LegacyGameStateImportCommandDTO;
import org.example.springboot.dto.command.LegacyTaskProgressDTO;
import org.example.springboot.dto.response.GameMapLevelStateResponseDTO;
import org.example.springboot.dto.response.GameMutationResponseDTO;
import org.example.springboot.dto.response.GameStateResponseDTO;
import org.example.springboot.dto.response.GameTaskProgressResponseDTO;
import org.example.springboot.dto.response.RewardLedgerItemResponseDTO;
import org.example.springboot.dto.response.WeeklyChoiceOptionResponseDTO;
import org.example.springboot.dto.response.WeeklyChoiceRewardResponseDTO;
import org.example.springboot.exception.BusinessException;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.DayOfWeek;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Service
public class GameStateService {
    private static final Map<String, Integer> MERIDIAN_ROUTE_TARGETS = Map.ofEntries(
            Map.entry("meridian-route-lung", 5),
            Map.entry("meridian-route-large-intestine", 4),
            Map.entry("meridian-route-stomach", 5),
            Map.entry("meridian-route-spleen", 5),
            Map.entry("meridian-route-heart", 4),
            Map.entry("meridian-route-small-intestine", 4),
            Map.entry("meridian-route-bladder", 5),
            Map.entry("meridian-route-kidney", 4),
            Map.entry("meridian-route-pericardium", 4),
            Map.entry("meridian-route-sanjiao", 5),
            Map.entry("meridian-route-gallbladder", 6),
            Map.entry("meridian-route-liver", 4),
            Map.entry("meridian-route-ren", 5),
            Map.entry("meridian-route-du", 5)
    );
    private static final String MERIDIAN_AGGREGATE_TASK = "meridian-river-completion";
    private static final int SAFETY_CASE_SCORE_REWARD = 10;
    private static final int SAFETY_CASE_UNLOCK_SCORE = 20;
    private static final int MAX_SAFETY_DRAFT_BYTES = 64 * 1024;
    private static final Set<String> SAFETY_DRAFT_PHASES = Set.of("quiz", "memory", "sort", "pledge");
    private static final Set<Integer> MATCH_MILESTONES = Set.of(50, 100, 200, 350, 500, 750, 1000);
    private static final Set<Integer> SORT_MILESTONES = Set.of(50, 100, 150, 200, 250, 300);
    private static final Set<String> CONSUMABLE_MATERIALS = Set.of(
            "bamboo-slip-shard", "apricot-kernel", "herbal-leaf", "meridian-star-sand",
            "acupoint-star-pearl", "copper-token", "mugwort-floss", "safety-bell", "star-compass"
    );
    private static final Map<String, List<MaterialCost>> AGENCY_ARCHIVE_COSTS = Map.of(
            "gate", List.of(new MaterialCost("bamboo-slip-shard", 3), new MaterialCost("copper-token", 1)),
            "star-wall", List.of(new MaterialCost("meridian-star-sand", 4)),
            "herb-cabinet", List.of(new MaterialCost("herbal-leaf", 5)),
            "display", List.of(new MaterialCost("acupoint-star-pearl", 2), new MaterialCost("copper-token", 2)),
            "bell-wall", List.of(new MaterialCost("safety-bell", 2)),
            "archive", List.of(new MaterialCost("bamboo-slip-shard", 3), new MaterialCost("star-compass", 1)),
            "roof", List.of(new MaterialCost("bamboo-slip-shard", 5), new MaterialCost("meridian-star-sand", 4))
    );
    private static final Map<String, Set<String>> AGENCY_ARCHIVE_PREREQUISITES = Map.of(
            "gate", Set.of(),
            "herb-cabinet", Set.of("gate"),
            "star-wall", Set.of("gate"),
            "bell-wall", Set.of("gate"),
            "display", Set.of("star-wall"),
            "archive", Set.of("herb-cabinet"),
            "roof", Set.of("display", "bell-wall", "archive")
    );
    private static final int WEEKLY_CHOICE_TARGET = 5;
    private static final Map<String, Integer> WEEKLY_CHOICE_OPTIONS = Map.of(
            "bamboo-slip-shard", 3,
            "apricot-kernel", 2,
            "meridian-star-sand", 2
    );

    @Resource
    private JdbcTemplate jdbcTemplate;

    @Resource
    private ObjectMapper objectMapper;

    @Resource
    private MainlineConfigService mainlineConfigService;

    @Resource
    private WriteOperationService writeOperationService;

    @Resource
    private AuditEventService auditEventService;

    @Transactional
    public void initializeUser(Long userId) {
        requireUser(userId);
        jdbcTemplate.update("""
                INSERT IGNORE INTO user_copper_man_profile
                (user_id, copper_tokens, star_sand, completed_cases)
                VALUES (?, 0, 0, 0)
                """, userId);
        // 注册/报到是首个地图节点的真实完成事件，不依赖 score 推断地图状态。
        completeTaskInternal(userId, "onboarding", "map.checkin", "registration", 1, 1, "SERVER");
        synchronizeCompletedSafetyCaseProgress(userId);
        auditEventService.record(userId, null, "INITIALIZE_GAME_STATE", "GAME_STATE", String.valueOf(userId),
                "SUCCESS", Map.of("source", "registration"));
    }

    /**
     * Repairs the score for accounts that completed the safety case before the
     * map progression reward was wired to the server task event.
     */
    @Transactional
    public void synchronizeCompletedSafetyCaseProgress(Long userId) {
        requireUser(userId);
        Integer completed = jdbcTemplate.queryForObject("""
                SELECT COUNT(*) FROM user_task_progress
                WHERE user_id = ? AND task_code = 'daily-safety-quiz'
                  AND status = 'COMPLETED'
                """, Integer.class, userId);
        if (completed == null || completed == 0) {
            return;
        }

        // Compatibility bridge: the former safety quiz was the old mainline
        // gate. Preserve already-earned progress without using it for future
        // unlock decisions.
        completeTaskInternal(userId, "safety", "main-safety-case", "lifetime", 1, 1, "COMPATIBILITY");

        Integer score = jdbcTemplate.queryForObject(
                "SELECT COALESCE(score, 0) FROM `user` WHERE id = ?", Integer.class, userId);
        if (score != null && score < SAFETY_CASE_UNLOCK_SCORE) {
            jdbcTemplate.update("UPDATE `user` SET score = ?, updated_at = CURRENT_TIMESTAMP WHERE id = ?",
                    SAFETY_CASE_UNLOCK_SCORE, userId);
        }
    }

    @Transactional(readOnly = true)
    public GameStateResponseDTO getState(Long userId) {
        requireUser(userId);
        Integer score = jdbcTemplate.queryForObject("SELECT COALESCE(score, 0) FROM `user` WHERE id = ?", Integer.class, userId);
        if (score == null) {
            throw new BusinessException("用户不存在");
        }

        GameStateResponseDTO state = new GameStateResponseDTO();
        state.setUserId(userId);
        state.setCurrentScore(score);

        List<GameTaskProgressResponseDTO> tasks = jdbcTemplate.query("""
                SELECT game_code, task_code, period_key, progress, target, status, completed_at, claimed_at, config_version
                FROM user_task_progress WHERE user_id = ? ORDER BY updated_at DESC
                """, (rs, rowNum) -> {
            GameTaskProgressResponseDTO item = new GameTaskProgressResponseDTO();
            item.setGameCode(rs.getString("game_code"));
            item.setTaskCode(rs.getString("task_code"));
            item.setPeriodKey(rs.getString("period_key"));
            item.setProgress(rs.getInt("progress"));
            item.setTarget(rs.getInt("target"));
            item.setStatus(rs.getString("status"));
            item.setCompletedAt(rs.getTimestamp("completed_at") == null ? null : rs.getTimestamp("completed_at").toLocalDateTime());
            item.setClaimedAt(rs.getTimestamp("claimed_at") == null ? null : rs.getTimestamp("claimed_at").toLocalDateTime());
            item.setConfigVersion((Integer) rs.getObject("config_version"));
            return item;
        }, userId);
        state.setTasks(tasks);

        Map<String, Integer> materials = new LinkedHashMap<>();
        jdbcTemplate.query("SELECT material_code, amount FROM user_material_balance WHERE user_id = ?", rs -> {
            materials.put(rs.getString("material_code"), rs.getInt("amount"));
        }, userId);
        state.setMaterials(materials);

        Map<String, Object> profile = jdbcTemplate.query("""
                SELECT copper_tokens, star_sand, completed_cases
                FROM user_copper_man_profile WHERE user_id = ?
                """, rs -> rs.next() ? Map.of(
                "copperTokens", rs.getInt("copper_tokens"),
                "starSand", rs.getInt("star_sand"),
                "completedCases", rs.getInt("completed_cases")) : Map.of(), userId);
        state.setCopperTokens((Integer) profile.getOrDefault("copperTokens", 0));
        state.setStarSand((Integer) profile.getOrDefault("starSand", 0));
        state.setCompletedCases((Integer) profile.getOrDefault("completedCases", 0));
        state.setAgencyArchiveIds(jdbcTemplate.queryForList("""
                SELECT archive_item_id FROM user_agency_archive
                WHERE user_id = ? ORDER BY repaired_at, archive_item_id
                """, String.class, userId));
        state.setLegacyImportCompleted(hasLegacyImport(userId));

        populateMapState(state, tasks);
        int completedMainlineCount = (int) state.getLevels().stream()
                .filter(item -> Boolean.TRUE.equals(item.getCompleted()))
                .count();
        int adventureLevel = Math.min(state.getLevels().size() + 1, completedMainlineCount + 1);
        state.setUserLevel(adventureLevel);
        state.setLevelName(adventureLevelNameFor(adventureLevel));
        state.setStateVersion(tasks.stream().mapToLong(item -> item.getCompletedAt() == null ? 0 : item.getCompletedAt().atZone(java.time.ZoneId.systemDefault()).toEpochSecond()).max().orElse(0));
        return state;
    }

    @Transactional(readOnly = true)
    public WeeklyChoiceRewardResponseDTO getWeeklyChoiceReward(Long userId, LocalDate day) {
        requireUser(userId);
        LocalDate weekStart = weekStart(day);
        int progress = weeklyLearningProgress(userId, weekStart);
        List<Map<String, Object>> claimedRows = jdbcTemplate.queryForList("""
                SELECT item_code, amount FROM reward_ledger
                WHERE user_id = ? AND grant_key = ?
                """, userId, weeklyChoiceGrantKey(weekStart));
        boolean claimed = !claimedRows.isEmpty();
        String selectedItemCode = claimed ? String.valueOf(claimedRows.get(0).get("item_code")) : null;
        List<WeeklyChoiceOptionResponseDTO> options = WEEKLY_CHOICE_OPTIONS.entrySet().stream()
                .sorted(Map.Entry.comparingByKey())
                .map(entry -> new WeeklyChoiceOptionResponseDTO(entry.getKey(), entry.getValue()))
                .toList();
        return new WeeklyChoiceRewardResponseDTO(
                weekStart.toString(), Math.min(progress, WEEKLY_CHOICE_TARGET), WEEKLY_CHOICE_TARGET,
                progress >= WEEKLY_CHOICE_TARGET && !claimed, claimed, selectedItemCode, options);
    }

    @Transactional
    public GameMutationResponseDTO claimWeeklyChoiceReward(
            Long userId, String operationKey, String itemCode, LocalDate day) {
        requireUser(userId);
        Integer amount = WEEKLY_CHOICE_OPTIONS.get(itemCode);
        if (amount == null) {
            throw new BusinessException("400", "这份奖励不在本周可选范围内");
        }
        LocalDate weekStart = weekStart(day);
        Map<String, Object> request = Map.of("weekKey", weekStart.toString(), "itemCode", itemCode);
        WriteOperationService.Claim claim = writeOperationService.claim(
                userId, operationKey, "WEEKLY_CHOICE_REWARD", request);
        if (claim.replay()) {
            GameStateResponseDTO state = getState(userId);
            return new GameMutationResponseDTO(claim.operationKey(), true, state.getStateVersion(), state);
        }
        Integer alreadyClaimed = jdbcTemplate.queryForObject("""
                SELECT COUNT(*) FROM reward_ledger WHERE user_id = ? AND grant_key = ?
                """, Integer.class, userId, weeklyChoiceGrantKey(weekStart));
        if (alreadyClaimed != null && alreadyClaimed > 0) {
            throw new BusinessException("409", "本周奖励已经选过啦，下周一会有新的选择");
        }
        if (weeklyLearningProgress(userId, weekStart) < WEEKLY_CHOICE_TARGET) {
            throw new BusinessException("400", "本周还需要完成更多学习任务才能选择奖励");
        }
        boolean granted = insertReward(userId, weeklyChoiceGrantKey(weekStart), "WEEKLY_CHOICE", "weekly-choice",
                "MATERIAL", itemCode, amount, 0, claim.operationKey());
        if (!granted) {
            throw new BusinessException("409", "本周奖励已经选过啦，下周一会有新的选择");
        }
        auditEventService.record(userId, null, "WEEKLY_CHOICE_REWARD", "REWARD", weekStart.toString(),
                "SUCCESS", Map.of("itemCode", itemCode, "amount", amount));
        GameStateResponseDTO state = getState(userId);
        GameMutationResponseDTO response = new GameMutationResponseDTO(
                claim.operationKey(), false, state.getStateVersion(), state);
        writeOperationService.complete(claim, response);
        return response;
    }

    @Transactional(readOnly = true)
    public List<RewardLedgerItemResponseDTO> listRewardLedger(Long userId, Integer requestedLimit) {
        requireUser(userId);
        int limit = Math.max(1, Math.min(requestedLimit == null ? 50 : requestedLimit, 100));
        return jdbcTemplate.query("""
                SELECT id, source, task_code, reward_type, item_code, amount, score_delta, created_at
                FROM reward_ledger WHERE user_id = ?
                ORDER BY created_at DESC, id DESC LIMIT ?
                """, (rs, rowNum) -> new RewardLedgerItemResponseDTO(
                rs.getLong("id"), rs.getString("source"), rs.getString("task_code"),
                rs.getString("reward_type"), rs.getString("item_code"), rs.getInt("amount"),
                rs.getInt("score_delta"), rs.getTimestamp("created_at").toLocalDateTime()), userId, limit);
    }

    private LocalDate weekStart(LocalDate day) {
        LocalDate safeDay = day == null ? LocalDate.now() : day;
        return safeDay.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
    }

    private String weeklyChoiceGrantKey(LocalDate weekStart) {
        return "weekly-choice:" + weekStart;
    }

    private int weeklyLearningProgress(Long userId, LocalDate weekStart) {
        Integer count = jdbcTemplate.queryForObject("""
                SELECT COUNT(*) FROM user_task_progress
                WHERE user_id = ? AND status = 'COMPLETED' AND source = 'SERVER'
                  AND task_code <> 'map.checkin'
                  AND completed_at >= ? AND completed_at < ?
                """, Integer.class, userId, weekStart.atStartOfDay(), weekStart.plusDays(7).atStartOfDay());
        return count == null ? 0 : count;
    }

    @Transactional(readOnly = true)
    public Map<String, Object> getSafetyDraft(Long userId) {
        requireUser(userId);
        List<String> rows = jdbcTemplate.queryForList(
                "SELECT draft_json FROM user_safety_game_draft WHERE user_id = ?",
                String.class, userId);
        if (rows.isEmpty()) return null;
        try {
            return objectMapper.readValue(rows.get(0), new TypeReference<>() {});
        } catch (JsonProcessingException error) {
            throw new BusinessException("500", "安全课堂进度读取失败");
        }
    }

    @Transactional
    public Map<String, Object> saveSafetyDraft(Long userId, Map<String, Object> draft) {
        requireUser(userId);
        if (draft == null || !Integer.valueOf(1).equals(asInteger(draft.get("version")))) {
            throw new BusinessException("400", "安全课堂进度版本无效");
        }
        String phase = String.valueOf(draft.getOrDefault("gamePhase", ""));
        if (!SAFETY_DRAFT_PHASES.contains(phase)) {
            throw new BusinessException("400", "安全课堂阶段无效");
        }
        try {
            String json = objectMapper.writeValueAsString(draft);
            if (json.getBytes(java.nio.charset.StandardCharsets.UTF_8).length > MAX_SAFETY_DRAFT_BYTES) {
                throw new BusinessException("400", "安全课堂进度数据过大");
            }
            jdbcTemplate.update("""
                    INSERT INTO user_safety_game_draft (user_id, draft_json)
                    VALUES (?, ?)
                    ON DUPLICATE KEY UPDATE draft_json = VALUES(draft_json), updated_at = CURRENT_TIMESTAMP
                    """, userId, json);
            return draft;
        } catch (JsonProcessingException error) {
            throw new BusinessException("400", "安全课堂进度格式无效");
        }
    }

    @Transactional
    public void clearSafetyDraft(Long userId) {
        requireUser(userId);
        jdbcTemplate.update("DELETE FROM user_safety_game_draft WHERE user_id = ?", userId);
    }

    private Integer asInteger(Object value) {
        if (value instanceof Number number) return number.intValue();
        try {
            return value == null ? null : Integer.valueOf(String.valueOf(value));
        } catch (NumberFormatException ignored) {
            return null;
        }
    }

    @Transactional
    public GameMutationResponseDTO applyEvent(Long userId, String operationKey, GameProgressEventCommandDTO command) {
        requireUser(userId);
        validateCommand(command);
        ensureMainlinePrerequisite(userId, command.getTaskCode());
        WriteOperationService.Claim claim = writeOperationService.claim(userId, operationKey, "GAME_PROGRESS_EVENT", command);
        if (claim.replay()) {
            GameStateResponseDTO state = getState(userId);
            return new GameMutationResponseDTO(claim.operationKey(), true, state.getStateVersion(), state);
        }

        String periodKey = command.getPeriodKey() == null || command.getPeriodKey().isBlank()
                ? "lifetime" : command.getPeriodKey();
        int configuredTarget = targetOf(command.getTaskCode());
        int configuredVersion = mainlineConfigService.publishedVersion();
        jdbcTemplate.update("""
                INSERT IGNORE INTO user_task_progress
                (user_id, game_code, task_code, period_key, progress, target, status, source, config_version)
                VALUES (?, ?, ?, ?, 0, ?, 'IN_PROGRESS', 'SERVER', ?)
                """, userId, command.getGameCode(), command.getTaskCode(), periodKey, configuredTarget, configuredVersion);

        Map<String, Object> current = jdbcTemplate.queryForMap("""
                SELECT progress, target, status, claimed_at, config_version FROM user_task_progress
                WHERE user_id = ? AND game_code = ? AND task_code = ? AND period_key = ? FOR UPDATE
                """, userId, command.getGameCode(), command.getTaskCode(), periodKey);
        int currentProgress = ((Number) current.get("progress")).intValue();
        int target = ((Number) current.get("target")).intValue();
        boolean wasCompleted = "COMPLETED".equals(current.get("status"));
        boolean routeTask = isMeridianRouteTask(command.getTaskCode());
        if (routeTask && "TASK_RESET".equals(command.getEventType())) {
            jdbcTemplate.update("""
                    UPDATE user_task_progress
                    SET progress = 0, status = 'IN_PROGRESS', completed_at = NULL, claimed_at = NULL,
                        version = version + 1, updated_at = CURRENT_TIMESTAMP
                    WHERE user_id = ? AND game_code = ? AND task_code = ? AND period_key = ?
                    """, userId, command.getGameCode(), command.getTaskCode(), periodKey);
            auditEventService.record(userId, null, "GAME_PROGRESS_RESET", "GAME_TASK", command.getTaskCode(),
                    "SUCCESS", Map.of("gameCode", command.getGameCode(), "periodKey", periodKey));
            GameStateResponseDTO state = getState(userId);
            GameMutationResponseDTO response = new GameMutationResponseDTO(claim.operationKey(), false, state.getStateVersion(), state);
            writeOperationService.complete(claim, response);
            return response;
        }
        boolean claimRoute = routeTask && "TASK_COMPLETED".equals(command.getEventType());
        if (claimRoute && currentProgress < target) {
            throw new BusinessException("400", "经络路线尚未完成，不能领取奖励");
        }
        int delta = command.getProgressDelta() == null ? 1 : command.getProgressDelta();
        int nextProgress = routeTask
                ? Math.min(target, currentProgress + (claimRoute ? 0 : delta))
                : ("TASK_COMPLETED".equals(command.getEventType()) ? target : Math.min(target, currentProgress + delta));
        boolean completed = nextProgress >= target;
        jdbcTemplate.update("""
                UPDATE user_task_progress
                SET progress = ?, status = ?, completed_at = CASE WHEN ? = 'COMPLETED' AND completed_at IS NULL THEN CURRENT_TIMESTAMP ELSE completed_at END,
                    version = version + 1, updated_at = CURRENT_TIMESTAMP
                WHERE user_id = ? AND game_code = ? AND task_code = ? AND period_key = ?
                """, nextProgress, completed ? "COMPLETED" : "IN_PROGRESS", completed ? "COMPLETED" : "IN_PROGRESS",
                userId, command.getGameCode(), command.getTaskCode(), periodKey);

        if (completed && !wasCompleted && !routeTask) {
            grantTaskRewards(userId, command.getTaskCode(), periodKey, claim.operationKey());
            if ("daily-safety-quiz".equals(command.getTaskCode()) && "lifetime".equals(periodKey)) {
                jdbcTemplate.update("""
                        UPDATE `user`
                        SET score = COALESCE(score, 0) + ?, updated_at = CURRENT_TIMESTAMP
                        WHERE id = ?
                        """, SAFETY_CASE_SCORE_REWARD, userId);
            }
        }
        if (routeTask && claimRoute && current.get("claimed_at") == null) {
            grantTaskRewards(userId, command.getTaskCode(), periodKey, claim.operationKey());
            jdbcTemplate.update("""
                    UPDATE user_task_progress SET claimed_at = CURRENT_TIMESTAMP,
                        version = version + 1, updated_at = CURRENT_TIMESTAMP
                    WHERE user_id = ? AND game_code = ? AND task_code = ? AND period_key = ?
                    """, userId, command.getGameCode(), command.getTaskCode(), periodKey);
        }
        if (routeTask && claimRoute) {
            syncMeridianAggregate(userId, claim.operationKey());
        }
        auditEventService.record(userId, null, "GAME_PROGRESS", "GAME_TASK", command.getTaskCode(),
                "SUCCESS", Map.of("gameCode", command.getGameCode(), "periodKey", periodKey, "progress", nextProgress));

        GameStateResponseDTO state = getState(userId);
        GameMutationResponseDTO response = new GameMutationResponseDTO(claim.operationKey(), false, state.getStateVersion(), state);
        writeOperationService.complete(claim, response);
        return response;
    }

    @Transactional
    public GameMutationResponseDTO importLegacyState(Long userId, String operationKey, LegacyGameStateImportCommandDTO command) {
        requireUser(userId);
        WriteOperationService.Claim claim = writeOperationService.claim(userId, operationKey, "GAME_LEGACY_IMPORT", command);
        if (claim.replay()) {
            GameStateResponseDTO state = getState(userId);
            return new GameMutationResponseDTO(claim.operationKey(), true, state.getStateVersion(), state);
        }

        String stateHash = writeOperationService.hash(command);
        int inserted = jdbcTemplate.update("""
                INSERT IGNORE INTO user_game_migration
                (user_id, source_key, source_version, state_hash, status, imported_at)
                VALUES (?, ?, ?, ?, 'COMPLETED', CURRENT_TIMESTAMP)
                """, userId, command.getSourceKey(), command.getSchemaVersion(), stateHash);
        if (inserted == 0) {
            GameStateResponseDTO state = getState(userId);
            GameMutationResponseDTO response = new GameMutationResponseDTO(claim.operationKey(), true, state.getStateVersion(), state);
            writeOperationService.complete(claim, response);
            return response;
        }

        for (LegacyTaskProgressDTO item : command.getTasks()) {
            if (!isKnownTask(item.getTaskCode())) {
                continue;
            }
            String periodKey = item.getPeriodKey() == null || item.getPeriodKey().isBlank() ? "lifetime" : item.getPeriodKey();
            int target = targetOf(item.getTaskCode());
            int progress = Boolean.TRUE.equals(item.getCompleted()) ? target : Math.min(target, Math.max(0, item.getProgress()));
            if (progress <= 0) {
                continue;
            }
            boolean rewardClaimed = Boolean.TRUE.equals(item.getRewardClaimed());
            String legacyGameCode = item.getTaskCode().startsWith("meridian") ? "meridian-river" : "legacy-import";
            jdbcTemplate.update("""
                    INSERT INTO user_task_progress
                    (user_id, game_code, task_code, period_key, progress, target, status, source, completed_at, claimed_at)
                    VALUES (?, ?, ?, ?, ?, ?, ?, 'LEGACY_IMPORT', CASE WHEN ? = 'COMPLETED' THEN CURRENT_TIMESTAMP ELSE NULL END,
                        CASE WHEN ? = 'COMPLETED' AND ? = TRUE THEN CURRENT_TIMESTAMP ELSE NULL END)
                    ON DUPLICATE KEY UPDATE progress = GREATEST(progress, VALUES(progress)),
                        status = CASE WHEN GREATEST(progress, VALUES(progress)) >= target THEN 'COMPLETED' ELSE status END,
                        completed_at = CASE WHEN GREATEST(progress, VALUES(progress)) >= target AND completed_at IS NULL THEN CURRENT_TIMESTAMP ELSE completed_at END,
                        claimed_at = CASE WHEN claimed_at IS NULL AND VALUES(claimed_at) IS NOT NULL THEN VALUES(claimed_at) ELSE claimed_at END,
                        version = version + 1, updated_at = CURRENT_TIMESTAMP
                    """, userId, legacyGameCode, item.getTaskCode(), periodKey, progress, target,
                    progress >= target ? "COMPLETED" : "IN_PROGRESS", progress >= target ? "COMPLETED" : "IN_PROGRESS",
                    rewardClaimed ? "COMPLETED" : "IN_PROGRESS", rewardClaimed);
            if (progress >= target && !rewardClaimed && !isMeridianRouteTask(item.getTaskCode())) {
                grantTaskRewards(userId, item.getTaskCode(), periodKey, "legacy:" + command.getSourceKey());
            }
        }
        synchronizeCompletedSafetyCaseProgress(userId);
        syncMeridianAggregate(userId, "legacy:" + command.getSourceKey());
        auditEventService.record(userId, null, "GAME_LEGACY_IMPORT", "GAME_STATE", String.valueOf(userId),
                "SUCCESS", Map.of("sourceKey", command.getSourceKey(), "schemaVersion", command.getSchemaVersion()));
        GameStateResponseDTO state = getState(userId);
        GameMutationResponseDTO response = new GameMutationResponseDTO(claim.operationKey(), false, state.getStateVersion(), state);
        writeOperationService.complete(claim, response);
        return response;
    }

    @Transactional
    public boolean completeCopperManCase(Long userId, LocalDate caseDate) {
        ensureMainlinePrerequisite(userId, "copper-man-daily-case");
        GameProgressEventCommandDTO command = new GameProgressEventCommandDTO();
        command.setGameCode("copper-man");
        command.setEventType("TASK_COMPLETED");
        command.setTaskCode("copper-man-daily-case");
        command.setPeriodKey(caseDate.toString());
        command.setProgressDelta(1);
        String key = "copper-man-case:" + caseDate;
        WriteOperationService.Claim claim = writeOperationService.claim(userId, key, "COPPER_MAN_CASE", command);
        if (claim.replay()) {
            return false;
        }
        jdbcTemplate.update("""
                INSERT INTO user_task_progress
                (user_id, game_code, task_code, period_key, progress, target, status, source, completed_at)
                VALUES (?, 'copper-man', 'copper-man-daily-case', ?, 1, 1, 'COMPLETED', 'SERVER', CURRENT_TIMESTAMP)
                ON DUPLICATE KEY UPDATE status = 'COMPLETED', progress = 1, completed_at = COALESCE(completed_at, CURRENT_TIMESTAMP)
                """, userId, caseDate.toString());
        Integer legacyCompleted = jdbcTemplate.queryForObject("""
                SELECT COUNT(*) FROM user_copper_man_profile
                WHERE user_id = ? AND last_completed_case_date IS NOT NULL AND last_completed_case_date >= ?
                """, Integer.class, userId, caseDate);
        if (legacyCompleted != null && legacyCompleted > 0) {
            insertReward(userId, "copper-man-daily-case:" + caseDate + ":copper-token", "COMPATIBILITY", "copper-man-daily-case",
                    "MATERIAL", "copper-token", 0, 0, claim.operationKey());
            insertReward(userId, "copper-man-daily-case:" + caseDate + ":meridian-star-sand", "COMPATIBILITY", "copper-man-daily-case",
                    "MATERIAL", "meridian-star-sand", 0, 0, claim.operationKey());
            writeOperationService.complete(claim, Map.of("completed", false, "legacyAlreadyCompleted", true));
            return false;
        }
        boolean newlyGranted = grantTaskRewards(userId, "copper-man-daily-case", caseDate.toString(), key);
        if (newlyGranted) {
            jdbcTemplate.update("""
                    UPDATE user_copper_man_profile
                    SET copper_tokens = copper_tokens + 2, star_sand = star_sand + 5,
                        completed_cases = completed_cases + 1, last_completed_case_date = ?
                    WHERE user_id = ? AND (last_completed_case_date IS NULL OR last_completed_case_date < ?)
                    """, caseDate, userId, caseDate);
        }
        writeOperationService.complete(claim, Map.of("completed", newlyGranted));
        return newlyGranted;
    }

    @Transactional
    public GameMutationResponseDTO exchangeAgencyArchiveItem(Long userId, String operationKey, String itemId) {
        requireUser(userId);
        ensureMainlinePrerequisite(userId, "main-repair-agency");
        List<MaterialCost> costs = AGENCY_ARCHIVE_COSTS.get(itemId);
        if (costs == null) {
            throw new BusinessException("400", "未知的侦探社修复项目");
        }
        Map<String, String> request = Map.of("itemId", itemId);
        WriteOperationService.Claim claim = writeOperationService.claim(userId, operationKey, "AGENCY_ARCHIVE_EXCHANGE", request);
        if (claim.replay()) {
            GameStateResponseDTO state = getState(userId);
            return new GameMutationResponseDTO(claim.operationKey(), true, state.getStateVersion(), state);
        }

        Integer owned = jdbcTemplate.queryForObject("""
                SELECT COUNT(*) FROM user_agency_archive WHERE user_id = ? AND archive_item_id = ?
                """, Integer.class, userId, itemId);
        if (owned != null && owned > 0) {
            throw new BusinessException("409", "该项目已经修复完成");
        }
        Set<String> prerequisites = AGENCY_ARCHIVE_PREREQUISITES.getOrDefault(itemId, Set.of());
        for (String prerequisite : prerequisites) {
            Integer prerequisiteOwned = jdbcTemplate.queryForObject("""
                    SELECT COUNT(*) FROM user_agency_archive WHERE user_id = ? AND archive_item_id = ?
                    """, Integer.class, userId, prerequisite);
            if (prerequisiteOwned == null || prerequisiteOwned == 0) {
                throw new BusinessException("409", "请先完成前面的侦探社修复项目");
            }
        }
        for (MaterialCost cost : costs) {
            int deducted = jdbcTemplate.update("""
                    UPDATE user_material_balance
                    SET amount = amount - ?, version = version + 1, updated_at = CURRENT_TIMESTAMP
                    WHERE user_id = ? AND material_code = ? AND amount >= ?
                    """, cost.amount(), userId, cost.materialCode(), cost.amount());
            if (deducted == 0) {
                throw new BusinessException("400", "修复材料不足");
            }
            jdbcTemplate.update("""
                    INSERT IGNORE INTO reward_ledger
                    (user_id, grant_key, source, task_code, reward_type, item_code, amount, score_delta, idempotency_key)
                    VALUES (?, ?, 'AGENCY_EXCHANGE', 'main-repair-agency', 'MATERIAL_SPEND', ?, ?, 0, ?)
                    """, userId, "agency:" + itemId + ":spend:" + cost.materialCode(),
                    cost.materialCode(), -cost.amount(), claim.operationKey());
        }
        jdbcTemplate.update("""
                INSERT INTO user_agency_archive (user_id, archive_item_id, repaired_at)
                VALUES (?, ?, CURRENT_TIMESTAMP)
                """, userId, itemId);

        Integer repairedCount = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM user_agency_archive WHERE user_id = ?", Integer.class, userId);
        int repairProgress = repairedCount == null ? 0 : repairedCount;
        // The gate opens the long-term agency loop; all seven repairs remain the full-case milestone.
        completeTaskInternal(userId, "agency", "main-repair-agency", "lifetime",
                "gate".equals(itemId) ? 1 : 1, 1, "SERVER");
        if (repairProgress == AGENCY_ARCHIVE_COSTS.size()) {
            grantTaskRewards(userId, "main-repair-agency", "lifetime", claim.operationKey());
        }
        auditEventService.record(userId, null, "AGENCY_ARCHIVE_EXCHANGE", "AGENCY_ARCHIVE", itemId,
                "SUCCESS", Map.of("costs", costs));
        GameStateResponseDTO state = getState(userId);
        GameMutationResponseDTO response = new GameMutationResponseDTO(claim.operationKey(), false, state.getStateVersion(), state);
        writeOperationService.complete(claim, response);
        return response;
    }

    @Transactional
    public boolean claimShuntingReward(Long userId, LocalDate rewardDate) {
        String key = "shunting:" + rewardDate;
        WriteOperationService.Claim claim = writeOperationService.claim(userId, key, "SHUNTING_REWARD", Map.of("rewardDate", rewardDate));
        if (claim.replay()) {
            return false;
        }
        jdbcTemplate.update("""
                INSERT INTO user_task_progress
                (user_id, game_code, task_code, period_key, progress, target, status, source, completed_at)
                VALUES (?, 'shunting', 'shunting-daily', ?, 1, 1, 'COMPLETED', 'SERVER', CURRENT_TIMESTAMP)
                ON DUPLICATE KEY UPDATE status = 'COMPLETED', progress = 1, completed_at = COALESCE(completed_at, CURRENT_TIMESTAMP)
                """, userId, rewardDate.toString());
        Integer legacyRewarded = jdbcTemplate.queryForObject("""
                SELECT COUNT(*) FROM user_game_reward
                WHERE user_id = ? AND game_code = 'shunting' AND reward_date = ?
                """, Integer.class, userId, rewardDate);
        if (legacyRewarded != null && legacyRewarded > 0) {
            insertReward(userId, "shunting:" + rewardDate, "COMPATIBILITY", "shunting-daily",
                    "SCORE", null, 0, 0, claim.operationKey());
            writeOperationService.complete(claim, Map.of("rewarded", false, "legacyAlreadyRewarded", true));
            return false;
        }
        boolean newlyGranted = insertReward(userId, "shunting:" + rewardDate, "SHUNTING", "shunting-daily",
                "SCORE", null, 0, 10, claim.operationKey());
        if (newlyGranted) {
            jdbcTemplate.update("UPDATE `user` SET score = COALESCE(score, 0) + 10, updated_at = CURRENT_TIMESTAMP WHERE id = ?", userId);
            jdbcTemplate.update("""
                    INSERT IGNORE INTO user_game_reward (user_id, game_code, reward_date, score_awarded, created_at)
                    VALUES (?, 'shunting', ?, 10, CURRENT_TIMESTAMP)
                    """, userId, rewardDate);
        }
        writeOperationService.complete(claim, Map.of("rewarded", newlyGranted, "scoreDelta", newlyGranted ? 10 : 0));
        return newlyGranted;
    }

    @Transactional
    public boolean checkinToday(Long userId, LocalDate day) {
        String key = "checkin:" + day;
        WriteOperationService.Claim claim = writeOperationService.claim(userId, key, "CHECKIN", Map.of("day", day));
        if (claim.replay()) {
            return false;
        }
        int inserted = jdbcTemplate.update("""
                INSERT IGNORE INTO user_checkin (user_id, checkin_date, created_at)
                VALUES (?, ?, CURRENT_TIMESTAMP)
                """, userId, day);
        if (inserted == 0) {
            writeOperationService.complete(claim, Map.of("rewarded", false));
            return false;
        }
        insertReward(userId, "checkin:" + day, "CHECKIN", "map.checkin", "SCORE", null, 0, 5, claim.operationKey());
        jdbcTemplate.update("UPDATE `user` SET score = COALESCE(score, 0) + 5, updated_at = CURRENT_TIMESTAMP WHERE id = ?", userId);
        completeTaskInternal(userId, "checkin", "map.checkin", day.toString(), 1, 1, "SERVER");
        writeOperationService.complete(claim, Map.of("rewarded", true, "scoreDelta", 5));
        return true;
    }

    private boolean grantTaskRewards(Long userId, String taskCode, String periodKey, String idempotencyKey) {
        boolean changed = false;
        int ruleVersion = taskRuleVersion(userId, taskCode, periodKey);
        List<org.example.springboot.dto.response.MainlineRewardConfigDTO> configuredRewards =
                mainlineConfigService.taskRewards(taskCode, ruleVersion);
        if (!configuredRewards.isEmpty() || mainlineConfigService.taskConfig(taskCode, ruleVersion) != null) {
            int index = 0;
            for (var reward : configuredRewards) {
                String rewardKey = taskCode + ":" + periodKey + ":" + reward.getRewardType() + ":" + index++;
                if ("SCORE".equals(reward.getRewardType())) {
                    changed |= insertReward(userId, rewardKey, "TASK", taskCode,
                            "SCORE", null, 0, reward.getScoreDelta() == null ? 0 : reward.getScoreDelta(), idempotencyKey);
                } else {
                    changed |= insertReward(userId, rewardKey, "TASK", taskCode,
                            "MATERIAL", reward.getItemCode(), reward.getAmount() == null ? 0 : reward.getAmount(), 0, idempotencyKey);
                }
            }
            return changed;
        }
        for (RewardSpec reward : rewardsFor(taskCode, ruleVersion)) {
            changed |= insertReward(userId, taskCode + ":" + periodKey + ":" + reward.itemCode(), "TASK", taskCode,
                    "MATERIAL", reward.itemCode(), reward.amount(), 0, idempotencyKey);
        }
        return changed;
    }

    private int taskRuleVersion(Long userId, String taskCode, String periodKey) {
        try {
            Integer version = jdbcTemplate.queryForObject("""
                    SELECT config_version FROM user_task_progress
                    WHERE user_id = ? AND task_code = ? AND period_key = ?
                    ORDER BY updated_at DESC LIMIT 1
                    """, Integer.class, userId, taskCode, periodKey);
            return version == null ? mainlineConfigService.publishedVersion() : version;
        } catch (DataAccessException error) {
            return mainlineConfigService.publishedVersion();
        }
    }

    private boolean insertReward(Long userId, String grantKey, String source, String taskCode,
                                 String rewardType, String itemCode, int amount, int scoreDelta, String idempotencyKey) {
        if (itemCode != null && amount != 0 && !CONSUMABLE_MATERIALS.contains(itemCode)) {
            throw new BusinessException("500", "奖励材料未注册");
        }
        int inserted = jdbcTemplate.update("""
                INSERT IGNORE INTO reward_ledger
                (user_id, grant_key, source, task_code, reward_type, item_code, amount, score_delta, idempotency_key)
                VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)
                """, userId, grantKey, source, taskCode, rewardType, itemCode, amount, scoreDelta, idempotencyKey);
        if (inserted == 0) {
            return false;
        }
        if (itemCode != null && amount != 0) {
            jdbcTemplate.update("""
                    INSERT INTO user_material_balance (user_id, material_code, amount, version)
                    VALUES (?, ?, ?, 0)
                    ON DUPLICATE KEY UPDATE amount = amount + VALUES(amount), version = version + 1, updated_at = CURRENT_TIMESTAMP
                    """, userId, itemCode, amount);
        }
        if (scoreDelta != 0) {
            jdbcTemplate.update("UPDATE `user` SET score = COALESCE(score, 0) + ?, updated_at = CURRENT_TIMESTAMP WHERE id = ?",
                    scoreDelta, userId);
        }
        return true;
    }

    private void completeTaskInternal(Long userId, String gameCode, String taskCode, String periodKey,
                                      int progress, int target, String source) {
        jdbcTemplate.update("""
                INSERT INTO user_task_progress
                (user_id, game_code, task_code, period_key, progress, target, status, source, completed_at, config_version)
                VALUES (?, ?, ?, ?, ?, ?, ?, ?, CASE WHEN ? = 'COMPLETED' THEN CURRENT_TIMESTAMP ELSE NULL END, ?)
                ON DUPLICATE KEY UPDATE progress = GREATEST(progress, VALUES(progress)),
                    status = CASE WHEN GREATEST(progress, VALUES(progress)) >= target THEN 'COMPLETED' ELSE status END,
                    completed_at = CASE WHEN GREATEST(progress, VALUES(progress)) >= target AND completed_at IS NULL THEN CURRENT_TIMESTAMP ELSE completed_at END,
                    version = version + 1, updated_at = CURRENT_TIMESTAMP
                """, userId, gameCode, taskCode, periodKey, progress, target,
                progress >= target ? "COMPLETED" : "IN_PROGRESS", source, progress >= target ? "COMPLETED" : "IN_PROGRESS",
                mainlineConfigService.publishedVersion());
    }

    private boolean isMeridianRouteTask(String taskCode) {
        return MERIDIAN_ROUTE_TARGETS.containsKey(taskCode);
    }

    private boolean isMeridianMatchTask(String taskCode) {
        if (taskCode == null || !taskCode.startsWith("meridian-match-")) return false;
        String[] parts = taskCode.split("-");
        if (parts.length != 4 || !Set.of("easy", "normal", "hard").contains(parts[2])) return false;
        try {
            return MATCH_MILESTONES.contains(Integer.parseInt(parts[3]));
        } catch (NumberFormatException ex) {
            return false;
        }
    }

    private boolean isMeridianSortTask(String taskCode) {
        if (taskCode == null || !taskCode.startsWith("meridian-sort-")) return false;
        try {
            return SORT_MILESTONES.contains(Integer.parseInt(taskCode.substring("meridian-sort-".length())));
        } catch (NumberFormatException ex) {
            return false;
        }
    }

    private boolean isKnownTask(String taskCode) {
        return mainlineConfigService.isKnownTask(taskCode)
                || isMeridianRouteTask(taskCode)
                || isMeridianMatchTask(taskCode)
                || isMeridianSortTask(taskCode);
    }

    private void ensureMainlinePrerequisite(Long userId, String taskCode) {
        String levelId = mainlineConfigService.levelIdForTask(taskCode, mainlineConfigService.publishedVersion());
        if (levelId == null) return;
        List<org.example.springboot.dto.response.MainlineLevelConfigDTO> levels = mainlineConfigService.runtimeLevels();
        Map<String, org.example.springboot.dto.response.MainlineLevelConfigDTO> byId = levels.stream()
                .collect(java.util.stream.Collectors.toMap(org.example.springboot.dto.response.MainlineLevelConfigDTO::getId, item -> item));
        Set<String> requiredLevels = new java.util.LinkedHashSet<>();
        collectPrerequisites(levelId, byId, requiredLevels);
        for (String requiredLevelId : requiredLevels) {
            var level = byId.get(requiredLevelId);
            for (String requiredTask : level.getRequiredTaskIds()) {
                Integer completed = jdbcTemplate.queryForObject("""
                        SELECT COUNT(*) FROM user_task_progress
                        WHERE user_id = ? AND task_code = ? AND status = 'COMPLETED'
                        """, Integer.class, userId, requiredTask);
                if (completed == null || completed == 0) throw new BusinessException("409", "请先完成前置关卡后再提交当前任务");
            }
        }
    }

    private void collectPrerequisites(String levelId,
                                      Map<String, org.example.springboot.dto.response.MainlineLevelConfigDTO> byId,
                                      Set<String> collected) {
        var level = byId.get(levelId);
        if (level == null) return;
        for (String prerequisite : level.getPrerequisiteLevelIds()) {
            if (collected.add(prerequisite)) collectPrerequisites(prerequisite, byId, collected);
        }
    }

    private void syncMeridianAggregate(Long userId, String idempotencyKey) {
        String sql = "SELECT COUNT(*) FROM user_task_progress "
                + "WHERE user_id = ? AND task_code IN (" + routeTaskPlaceholders() + ") "
                + "AND status = 'COMPLETED' AND claimed_at IS NOT NULL AND period_key = 'lifetime'";
        int completedRoutes = jdbcTemplate.queryForObject(sql, Integer.class, routeTaskParams(userId));
        int aggregateTarget = targetOf(MERIDIAN_AGGREGATE_TASK);
        if (completedRoutes < aggregateTarget) return;

        completeTaskInternal(userId, "meridian-river", MERIDIAN_AGGREGATE_TASK, "lifetime",
                completedRoutes, aggregateTarget, "SERVER");
        Integer legacyRewardCount = jdbcTemplate.queryForObject("""
                SELECT COUNT(*) FROM reward_ledger
                WHERE user_id = ? AND task_code = 'meridian-route' AND amount > 0
                """, Integer.class, userId);
        if (legacyRewardCount != null && legacyRewardCount > 0) return;
        grantTaskRewards(userId, MERIDIAN_AGGREGATE_TASK, "lifetime", idempotencyKey + ":aggregate");
    }

    private String routeTaskPlaceholders() {
        return MERIDIAN_ROUTE_TARGETS.keySet().stream().map(item -> "?").collect(java.util.stream.Collectors.joining(","));
    }

    private Object[] routeTaskParams(Long userId) {
        Object[] params = new Object[MERIDIAN_ROUTE_TARGETS.size() + 1];
        params[0] = userId;
        int index = 1;
        for (String taskCode : MERIDIAN_ROUTE_TARGETS.keySet()) params[index++] = taskCode;
        return params;
    }

    private void populateMapState(GameStateResponseDTO state, List<GameTaskProgressResponseDTO> tasks) {
        Set<String> completedTasks = tasks.stream()
                .filter(item -> "COMPLETED".equals(item.getStatus()))
                .map(GameTaskProgressResponseDTO::getTaskCode)
                .collect(java.util.stream.Collectors.toSet());
        Map<String, Integer> progressByTask = new LinkedHashMap<>();
        Map<String, Integer> targetByTask = new LinkedHashMap<>();
        tasks.forEach(item -> progressByTask.merge(item.getTaskCode(),
                item.getProgress() == null ? 0 : item.getProgress(), Math::max));
        tasks.forEach(item -> targetByTask.putIfAbsent(item.getTaskCode(), item.getTarget()));
        List<GameMapLevelStateResponseDTO> levels = new ArrayList<>();
        int firstCurrent = -1;
        List<org.example.springboot.dto.response.MainlineLevelConfigDTO> maps = mainlineConfigService.runtimeLevels();
        Map<String, Boolean> completedByLevel = new LinkedHashMap<>();
        for (var map : maps) {
            boolean unlocked = map.getPrerequisiteLevelIds().isEmpty()
                    || map.getPrerequisiteLevelIds().stream().allMatch(prerequisite -> Boolean.TRUE.equals(completedByLevel.get(prerequisite)));
            boolean completed = unlocked && map.getRequiredTaskIds().stream().allMatch(completedTasks::contains);
            completedByLevel.put(map.getId(), completed);
            if (unlocked && !completed && firstCurrent < 0) {
                firstCurrent = map.getOrder();
            }
            GameMapLevelStateResponseDTO item = new GameMapLevelStateResponseDTO();
            item.setId(map.getId());
            item.setOrder(map.getOrder());
            item.setLabel(map.getLabel());
            item.setRoute(map.getRoute());
            item.setUnlocked(unlocked);
            item.setCompleted(completed);
            item.setProgress(map.getRequiredTaskIds().stream()
                    .mapToInt(task -> Math.min(targetByTask.getOrDefault(task, targetOf(task)), progressByTask.getOrDefault(task, 0)))
                    .sum());
            item.setTarget(map.getRequiredTaskIds().stream().mapToInt(task -> targetByTask.getOrDefault(task, targetOf(task))).sum());
            item.setRequiredTaskIds(List.copyOf(map.getRequiredTaskIds()));
            levels.add(item);
        }
        if (firstCurrent < 0) {
            firstCurrent = maps.get(maps.size() - 1).getOrder();
        }
        for (int index = 0; index < levels.size(); index++) {
            GameMapLevelStateResponseDTO item = levels.get(index);
            item.setCurrent(item.getOrder() == firstCurrent);
            item.setStatus(Boolean.TRUE.equals(item.getCompleted())
                    ? "COMPLETED"
                    : Boolean.TRUE.equals(item.getCurrent()) ? "CURRENT"
                    : Boolean.TRUE.equals(item.getUnlocked()) ? "UNLOCKED" : "LOCKED");
            item.setNextLevelId(index + 1 < levels.size() ? levels.get(index + 1).getId() : null);
        }
        state.setLevels(levels);
        final int currentOrder = firstCurrent;
        maps.stream().filter(map -> map.getOrder() == currentOrder).findFirst().ifPresent(map -> {
            state.setCurrentLevelId(map.getId());
            state.setCurrentMapKey(map.getMapKey());
        });
    }

    private boolean hasLegacyImport(Long userId) {
        Integer count = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM user_game_migration WHERE user_id = ? AND status = 'COMPLETED'", Integer.class, userId);
        return count != null && count > 0;
    }

    private void validateCommand(GameProgressEventCommandDTO command) {
        if (!Set.of("TASK_PROGRESS", "TASK_COMPLETED", "TASK_RESET", "CHECKIN_COMPLETED", "SHUNTING_COMPLETED").contains(command.getEventType())) {
            throw new BusinessException("400", "不支持的游戏事件类型");
        }
        if (!isKnownTask(command.getTaskCode())) {
            throw new BusinessException("400", "不支持的游戏任务");
        }
        if (MERIDIAN_AGGREGATE_TASK.equals(command.getTaskCode())) {
            throw new BusinessException("400", "星河总奖励需完成全部路线后由系统发放");
        }
        if ("meridian-route".equals(command.getTaskCode())) {
            throw new BusinessException("400", "旧版经络任务已停止提交，请按路线完成");
        }
        if ("CHECKIN_COMPLETED".equals(command.getEventType()) && !"map.checkin".equals(command.getTaskCode())) {
            throw new BusinessException("400", "签到事件与任务不匹配");
        }
        if ("SHUNTING_COMPLETED".equals(command.getEventType()) && !"shunting-daily".equals(command.getTaskCode())) {
            throw new BusinessException("400", "小游戏事件与任务不匹配");
        }
        if (command.getProgressDelta() != null && command.getProgressDelta() > 1
                && !"TASK_COMPLETED".equals(command.getEventType())) {
            throw new BusinessException("400", "单次游戏事件最多推进一个进度");
        }
        Set<String> acceptedResults = switch (command.getTaskCode()) {
            case "daily-read-copper-story" -> Set.of("STORY_COMPLETE", "CLIENT_COMPLETION");
            case "daily-light-hand-stars" -> Set.of("ACUPOINTS_COMPLETE", "CLIENT_COMPLETION");
            case "daily-safety-quiz" -> Set.of("QUIZ_COMPLETE", "CLIENT_COMPLETION");
            case "main-safety-case" -> Set.of("SAFETY_COMPLETE", "CLIENT_COMPLETION");
            case "main-hand-star-map" -> Set.of("BODY_REGION_COMPLETE", "BODY_POINT_COMPLETE", "TASK_COMPLETE", "CLIENT_COMPLETION");
            case "meridian-route", MERIDIAN_AGGREGATE_TASK -> Set.of("ROUTE_COMPLETE", "CLIENT_COMPLETION");
            case "copper-man-daily-case" -> Set.of("CASE_COMPLETE", "CLIENT_COMPLETION");
            case "shunting-daily" -> Set.of("SHUNTING_COMPLETE", "CLIENT_COMPLETION");
            case "review-daily" -> Set.of("REVIEW_COMPLETE", "CLIENT_COMPLETION");
            default -> Set.of("TASK_COMPLETE", "CLIENT_COMPLETION");
        };
        if (isMeridianRouteTask(command.getTaskCode())) {
            acceptedResults = Set.of("ROUTE_POINT_COMPLETE", "ROUTE_COMPLETE", "CLIENT_COMPLETION");
            if ("TASK_RESET".equals(command.getEventType())) {
                acceptedResults = Set.of("ROUTE_RESET");
            }
        } else if (isMeridianMatchTask(command.getTaskCode())) {
            acceptedResults = Set.of("MATCH_MILESTONE", "CLIENT_COMPLETION");
        } else if (isMeridianSortTask(command.getTaskCode())) {
            acceptedResults = Set.of("SORT_MILESTONE", "CLIENT_COMPLETION");
        }
        if (command.getResultCode() == null || !acceptedResults.contains(command.getResultCode())) {
            throw new BusinessException("400", "游戏结果不符合后端任务规则");
        }
    }

    private void requireUser(Long userId) {
        if (userId == null) {
            throw new BusinessException("401", "请先登录");
        }
    }

    private int targetOf(String taskCode) {
        Integer routeTarget = MERIDIAN_ROUTE_TARGETS.get(taskCode);
        if (routeTarget != null) return routeTarget;
        if (isMeridianMatchTask(taskCode) || isMeridianSortTask(taskCode)) return 1;
        return mainlineConfigService.taskTarget(taskCode, mainlineConfigService.publishedVersion());
    }

    private String adventureLevelNameFor(int adventureLevel) {
        if (adventureLevel >= 9) return "杏林金牌侦探";
        if (adventureLevel >= 5) return "经络追踪侦探";
        if (adventureLevel >= 3) return "身体地图侦探";
        return "铜人见习侦探";
    }

    private List<RewardSpec> rewardsFor(String taskCode) {
        return rewardsFor(taskCode, mainlineConfigService.publishedVersion());
    }

    private List<RewardSpec> rewardsFor(String taskCode, int ruleVersion) {
        if (isMeridianRouteTask(taskCode)) {
            return List.of(new RewardSpec("meridian-star-sand", 1), new RewardSpec("copper-token", 1));
        }
        if (MERIDIAN_AGGREGATE_TASK.equals(taskCode)) {
            return List.of(new RewardSpec("meridian-star-sand", 5), new RewardSpec("copper-token", 2));
        }
        if (isMeridianMatchTask(taskCode)) {
            String[] parts = taskCode.split("-");
            int score = Integer.parseInt(parts[3]);
            double multiplier = "hard".equals(parts[2]) ? 1.5 : 1.0;
            return List.of(matchReward(score, multiplier));
        }
        if (isMeridianSortTask(taskCode)) {
            int score = Integer.parseInt(taskCode.substring("meridian-sort-".length()));
            return List.of(sortReward(score));
        }
        return mainlineConfigService.taskRewards(taskCode, ruleVersion).stream()
                .filter(reward -> "MATERIAL".equals(reward.getRewardType()))
                .map(reward -> new RewardSpec(reward.getItemCode(), reward.getAmount()))
                .toList();
    }

    private RewardSpec matchReward(int score, double multiplier) {
        return switch (score) {
            case 50 -> new RewardSpec("meridian-star-sand", scaled(2, multiplier));
            case 100 -> new RewardSpec("herbal-leaf", scaled(3, multiplier));
            case 200 -> new RewardSpec("apricot-kernel", scaled(2, multiplier));
            case 350 -> new RewardSpec("bamboo-slip-shard", scaled(2, multiplier));
            case 500 -> new RewardSpec("meridian-star-sand", scaled(5, multiplier));
            case 750 -> new RewardSpec("herbal-leaf", scaled(5, multiplier));
            case 1000 -> new RewardSpec("apricot-kernel", scaled(5, multiplier));
            default -> throw new BusinessException("400", "未知的消消看奖励里程碑");
        };
    }

    private RewardSpec sortReward(int score) {
        return switch (score) {
            case 50 -> new RewardSpec("meridian-star-sand", 1);
            case 100 -> new RewardSpec("herbal-leaf", 2);
            case 150 -> new RewardSpec("apricot-kernel", 1);
            case 200 -> new RewardSpec("bamboo-slip-shard", 2);
            case 250 -> new RewardSpec("meridian-star-sand", 3);
            case 300 -> new RewardSpec("herbal-leaf", 3);
            default -> throw new BusinessException("400", "未知的穴位排序奖励里程碑");
        };
    }

    private int scaled(int amount, double multiplier) {
        return (int) Math.round(amount * multiplier);
    }

    private record RewardSpec(String itemCode, int amount) {}
    private record MaterialCost(String materialCode, int amount) {}
}
