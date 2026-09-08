package org.example.springboot.service;

import org.example.springboot.dto.command.GameProgressEventCommandDTO;
import org.example.springboot.dto.command.LegacyGameStateImportCommandDTO;
import org.example.springboot.dto.command.LegacyTaskProgressDTO;
import org.example.springboot.exception.BusinessException;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.context.annotation.Bean;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import jakarta.annotation.Resource;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@JdbcTest(properties = {
        "spring.datasource.url=jdbc:h2:mem:game-state;MODE=MySQL;DB_CLOSE_DELAY=-1;DATABASE_TO_LOWER=TRUE;NON_KEYWORDS=USER",
        "spring.datasource.driver-class-name=org.h2.Driver",
        "spring.datasource.username=sa",
        "spring.datasource.password=",
        "spring.sql.init.mode=never",
        "spring.flyway.enabled=false"
})
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import({GameStateService.class, MainlineConfigService.class, WriteOperationService.class, AuditEventService.class, GameStateServiceIdempotencyTest.Config.class})
@Sql(scripts = "/schema-copper-man-h2.sql")
class GameStateServiceIdempotencyTest {
    @TestConfiguration
    static class Config {
        @Bean
        ObjectMapper objectMapper() {
            return new ObjectMapper().registerModule(new JavaTimeModule());
        }
    }

    @Resource
    private GameStateService gameStateService;

    @Resource
    private JdbcTemplate jdbcTemplate;

    private void completeTask(String taskCode, int progress, int target) {
        jdbcTemplate.update("""
                INSERT INTO user_task_progress
                (user_id, game_code, task_code, period_key, progress, target, status, source, completed_at)
                VALUES (99001, 'test', ?, 'lifetime', ?, ?, 'COMPLETED', 'TEST', CURRENT_TIMESTAMP)
                """, taskCode, progress, target);
    }

    private void prepareForMeridian() {
        completeTask("map.checkin", 1, 1);
        completeTask("main-safety-case", 1, 1);
        completeTask("main-mist-in-xinglin", 1, 1);
        completeTask("main-hand-star-map", 10, 10);
    }

    @Test
    void repeatedCompletionReplaysWithoutDuplicatingRewards() {
        GameProgressEventCommandDTO command = new GameProgressEventCommandDTO();
        command.setGameCode("xinglin");
        command.setEventType("TASK_COMPLETED");
        command.setTaskCode("daily-read-copper-story");
        command.setPeriodKey("lifetime");
        command.setResultCode("STORY_COMPLETE");

        var first = gameStateService.applyEvent(99001L, "qa-story-once", command);
        var replay = gameStateService.applyEvent(99001L, "qa-story-once", command);

        assertThat(first.getIdempotentReplay()).isFalse();
        assertThat(replay.getIdempotentReplay()).isTrue();
        assertThat(jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM write_operation WHERE user_id = 99001 AND operation_key = 'qa-story-once'", Integer.class))
                .isEqualTo(1);
        assertThat(jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM reward_ledger WHERE user_id = 99001 AND task_code = 'daily-read-copper-story'", Integer.class))
                .isEqualTo(2);
        assertThat(jdbcTemplate.queryForObject(
                "SELECT amount FROM user_material_balance WHERE user_id = 99001 AND material_code = 'bamboo-slip-shard'", Integer.class))
                .isEqualTo(3);
    }

    @Test
    void safetyDraftIsStoredPerUserOverwrittenAndCleared() {
        Map<String, Object> first = Map.of(
                "version", 1,
                "gamePhase", "quiz",
                "totalScore", 100,
                "quiz", Map.of("index", 2, "lives", 3));
        Map<String, Object> second = Map.of(
                "version", 1,
                "gamePhase", "memory",
                "totalScore", 240,
                "memory", Map.of("matchedCount", 2));

        gameStateService.saveSafetyDraft(99001L, first);
        assertThat(gameStateService.getSafetyDraft(99001L))
                .containsEntry("gamePhase", "quiz")
                .containsEntry("totalScore", 100);

        gameStateService.saveSafetyDraft(99001L, second);
        assertThat(gameStateService.getSafetyDraft(99001L))
                .containsEntry("gamePhase", "memory")
                .containsEntry("totalScore", 240);
        assertThat(jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM user_safety_game_draft WHERE user_id = 99001", Integer.class))
                .isEqualTo(1);

        gameStateService.clearSafetyDraft(99001L);
        assertThat(gameStateService.getSafetyDraft(99001L)).isNull();
    }

    @Test
    void completingSafetyCaseAdvancesToTheNextMapAndIsScoreIdempotent() {
        gameStateService.initializeUser(99001L);
        jdbcTemplate.update("UPDATE `user` SET score = 10 WHERE id = 99001");

        GameProgressEventCommandDTO mainline = new GameProgressEventCommandDTO();
        mainline.setGameCode("xinglin");
        mainline.setEventType("TASK_COMPLETED");
        mainline.setTaskCode("main-safety-case");
        mainline.setPeriodKey("lifetime");
        mainline.setResultCode("SAFETY_COMPLETE");
        gameStateService.applyEvent(99001L, "qa-main-safety-case", mainline);

        GameProgressEventCommandDTO command = new GameProgressEventCommandDTO();
        command.setGameCode("xinglin");
        command.setEventType("TASK_COMPLETED");
        command.setTaskCode("daily-safety-quiz");
        command.setPeriodKey("lifetime");
        command.setResultCode("QUIZ_COMPLETE");

        var first = gameStateService.applyEvent(99001L, "qa-safety-case-once", command);
        var replay = gameStateService.applyEvent(99001L, "qa-safety-case-once", command);

        assertThat(first.getState().getCurrentLevelId()).isEqualTo("bamboo");
        assertThat(first.getState().getUserLevel()).isEqualTo(3);
        assertThat(first.getState().getLevels().get(1).getCompleted()).isTrue();
        assertThat(first.getState().getLevels().get(2).getUnlocked()).isTrue();
        assertThat(first.getState().getLevels().get(2).getCurrent()).isTrue();
        assertThat(first.getState().getLevels().get(3).getUnlocked()).isFalse();
        assertThat(replay.getState().getCurrentLevelId()).isEqualTo("bamboo");
        assertThat(jdbcTemplate.queryForObject(
                "SELECT score FROM `user` WHERE id = 99001", Integer.class)).isEqualTo(20);
    }

    @Test
    void previouslyCompletedSafetyCaseRepairsTheLegacyMapScore() {
        gameStateService.initializeUser(99001L);
        jdbcTemplate.update("UPDATE `user` SET score = 10 WHERE id = 99001");
        jdbcTemplate.update("""
                INSERT INTO user_task_progress
                (user_id, game_code, task_code, period_key, progress, target, status, source, completed_at)
                VALUES (99001, 'xinglin', 'daily-safety-quiz', 'lifetime', 3, 3, 'COMPLETED', 'SERVER', CURRENT_TIMESTAMP)
                """);

        gameStateService.synchronizeCompletedSafetyCaseProgress(99001L);
        var state = gameStateService.getState(99001L);

        assertThat(state.getCurrentLevelId()).isEqualTo("bamboo");
        assertThat(state.getLevels().get(1).getCompleted()).isTrue();
        assertThat(jdbcTemplate.queryForObject(
                "SELECT score FROM `user` WHERE id = 99001", Integer.class)).isEqualTo(20);
    }

    @Test
    void scoreCannotSkipTheCanonicalMainline() {
        gameStateService.initializeUser(99001L);
        jdbcTemplate.update("UPDATE `user` SET score = 999 WHERE id = 99001");

        var state = gameStateService.getState(99001L);

        assertThat(state.getCurrentLevelId()).isEqualTo("safe-start");
        assertThat(state.getUserLevel()).isEqualTo(2);
        assertThat(state.getLevels().get(1).getUnlocked()).isTrue();
        assertThat(state.getLevels().get(2).getUnlocked()).isFalse();
    }

    @Test
    void adventureLevelReachesNineOnlyAfterAllMainlineMapsAreComplete() {
        gameStateService.initializeUser(99001L);
        jdbcTemplate.update("UPDATE `user` SET score = 999 WHERE id = 99001");
        completeTask("main-safety-case", 1, 1);
        completeTask("main-mist-in-xinglin", 1, 1);
        completeTask("main-hand-star-map", 10, 10);
        completeTask("meridian-river-completion", 14, 14);
        completeTask("copper-man-daily-case", 1, 1);
        completeTask("review-daily", 1, 1);
        completeTask("main-repair-agency", 1, 1);

        var state = gameStateService.getState(99001L);

        assertThat(state.getUserLevel()).isEqualTo(9);
        assertThat(state.getLevelName()).isEqualTo("杏林金牌侦探");
        assertThat(state.getCurrentLevelId()).isEqualTo("agency");
        assertThat(state.getLevels()).allMatch(level -> Boolean.TRUE.equals(level.getCompleted()));
    }

    @Test
    void partialBodyProgressDoesNotCompleteOrUnlockMeridian() {
        gameStateService.initializeUser(99001L);
        completeTask("main-safety-case", 1, 1);
        completeTask("main-mist-in-xinglin", 1, 1);

        GameProgressEventCommandDTO progress = new GameProgressEventCommandDTO();
        progress.setGameCode("xinglin");
        progress.setEventType("TASK_PROGRESS");
        progress.setTaskCode("main-hand-star-map");
        progress.setPeriodKey("lifetime");
        progress.setResultCode("BODY_POINT_COMPLETE");
        progress.setProgressDelta(1);
        gameStateService.applyEvent(99001L, "qa-body-partial", progress);

        var state = gameStateService.getState(99001L);

        assertThat(state.getLevels().get(3).getCompleted()).isFalse();
        assertThat(state.getLevels().get(4).getUnlocked()).isFalse();
        assertThat(state.getLevels().get(3).getProgress()).isEqualTo(1);
        assertThat(state.getLevels().get(3).getTarget()).isEqualTo(10);
    }

    @Test
    void bodyRegionProgressUsesItsOwnAcceptedResultCode() {
        gameStateService.initializeUser(99001L);
        completeTask("main-safety-case", 1, 1);
        completeTask("main-mist-in-xinglin", 1, 1);

        GameProgressEventCommandDTO progress = new GameProgressEventCommandDTO();
        progress.setGameCode("xinglin");
        progress.setEventType("TASK_PROGRESS");
        progress.setTaskCode("main-hand-star-map");
        progress.setPeriodKey("lifetime");
        progress.setResultCode("BODY_REGION_COMPLETE");
        progress.setProgressDelta(1);

        gameStateService.applyEvent(99001L, "qa-body-region", progress);

        assertThat(gameStateService.getState(99001L).getLevels().get(3).getProgress()).isEqualTo(1);
    }

    @Test
    void reusingAnIdempotencyKeyWithDifferentPayloadIsRejected() {
        GameProgressEventCommandDTO first = new GameProgressEventCommandDTO();
        first.setGameCode("xinglin");
        first.setEventType("TASK_COMPLETED");
        first.setTaskCode("daily-read-copper-story");
        first.setPeriodKey("lifetime");
        first.setResultCode("STORY_COMPLETE");
        gameStateService.applyEvent(99001L, "qa-conflict", first);

        GameProgressEventCommandDTO changed = new GameProgressEventCommandDTO();
        changed.setGameCode("xinglin");
        changed.setEventType("TASK_COMPLETED");
        changed.setTaskCode("daily-safety-quiz");
        changed.setPeriodKey("lifetime");
        changed.setResultCode("QUIZ_COMPLETE");

        assertThatThrownBy(() -> gameStateService.applyEvent(99001L, "qa-conflict", changed))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("幂等键");
    }

    @Test
    void tenConcurrentSubmissionsCreateOneRewardGrant() throws Exception {
        GameProgressEventCommandDTO command = new GameProgressEventCommandDTO();
        command.setGameCode("xinglin");
        command.setEventType("TASK_COMPLETED");
        command.setTaskCode("daily-read-copper-story");
        command.setPeriodKey("concurrent");
        command.setResultCode("STORY_COMPLETE");

        ExecutorService executor = Executors.newFixedThreadPool(10);
        try {
            List<Future<?>> futures = new ArrayList<>();
            for (int i = 0; i < 10; i++) {
                futures.add(executor.submit(() -> gameStateService.applyEvent(99001L, "qa-concurrent-once", command)));
            }
            for (Future<?> future : futures) {
                future.get(10, TimeUnit.SECONDS);
            }
        } finally {
            executor.shutdownNow();
        }

        assertThat(jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM write_operation WHERE user_id = 99001 AND operation_key = 'qa-concurrent-once'", Integer.class))
                .isEqualTo(1);
        assertThat(jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM reward_ledger WHERE user_id = 99001 AND task_code = 'daily-read-copper-story'", Integer.class))
                .isEqualTo(2);
        assertThat(jdbcTemplate.queryForObject(
                "SELECT amount FROM user_material_balance WHERE user_id = 99001 AND material_code = 'bamboo-slip-shard'", Integer.class))
                .isEqualTo(3);
    }

    @Test
    void meridianRouteProgressWaitsForClaimAndIsIdempotent() {
        prepareForMeridian();
        for (int point = 1; point <= 5; point++) {
            GameProgressEventCommandDTO progress = new GameProgressEventCommandDTO();
            progress.setGameCode("meridian-river");
            progress.setEventType("TASK_PROGRESS");
            progress.setTaskCode("meridian-route-lung");
            progress.setPeriodKey("lifetime");
            progress.setResultCode("ROUTE_POINT_COMPLETE");
            progress.setProgressDelta(1);
            gameStateService.applyEvent(99001L, "qa-meridian-point-" + point, progress);
        }

        assertThat(jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM reward_ledger WHERE user_id = 99001 AND task_code = 'meridian-route-lung'", Integer.class))
                .isZero();

        GameProgressEventCommandDTO claim = new GameProgressEventCommandDTO();
        claim.setGameCode("meridian-river");
        claim.setEventType("TASK_COMPLETED");
        claim.setTaskCode("meridian-route-lung");
        claim.setPeriodKey("lifetime");
        claim.setResultCode("ROUTE_COMPLETE");
        claim.setProgressDelta(1);

        var first = gameStateService.applyEvent(99001L, "qa-meridian-claim", claim);
        var replay = gameStateService.applyEvent(99001L, "qa-meridian-claim", claim);

        assertThat(first.getIdempotentReplay()).isFalse();
        assertThat(replay.getIdempotentReplay()).isTrue();
        assertThat(jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM reward_ledger WHERE user_id = 99001 AND task_code = 'meridian-route-lung'", Integer.class))
                .isEqualTo(2);
        assertThat(jdbcTemplate.queryForObject(
                "SELECT amount FROM user_material_balance WHERE user_id = 99001 AND material_code = 'meridian-star-sand'", Integer.class))
                .isEqualTo(1);
    }

    @Test
    void meridianRouteCannotClaimBeforeAllPointsAreSaved() {
        prepareForMeridian();
        GameProgressEventCommandDTO claim = new GameProgressEventCommandDTO();
        claim.setGameCode("meridian-river");
        claim.setEventType("TASK_COMPLETED");
        claim.setTaskCode("meridian-route-lung");
        claim.setPeriodKey("lifetime");
        claim.setResultCode("ROUTE_COMPLETE");

        assertThatThrownBy(() -> gameStateService.applyEvent(99001L, "qa-meridian-early-claim", claim))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("尚未完成");
    }

    @Test
    void legacyCompletedButUnclaimedRouteWaitsForExplicitClaim() {
        prepareForMeridian();
        LegacyTaskProgressDTO route = new LegacyTaskProgressDTO();
        route.setTaskCode("meridian-route-lung");
        route.setProgress(5);
        route.setCompleted(true);
        route.setRewardClaimed(false);

        LegacyGameStateImportCommandDTO migration = new LegacyGameStateImportCommandDTO();
        migration.setSchemaVersion(4);
        migration.setSourceKey("xinglin-game-state-v4-unclaimed");
        migration.setTasks(List.of(route));
        gameStateService.importLegacyState(99001L, "qa-meridian-legacy-import", migration);

        assertThat(jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM reward_ledger WHERE user_id = 99001 AND task_code = 'meridian-route-lung'", Integer.class))
                .isZero();
        assertThat(jdbcTemplate.queryForObject(
                "SELECT claimed_at FROM user_task_progress WHERE user_id = 99001 AND task_code = 'meridian-route-lung'", Object.class))
                .isNull();

        GameProgressEventCommandDTO claim = new GameProgressEventCommandDTO();
        claim.setGameCode("meridian-river");
        claim.setEventType("TASK_COMPLETED");
        claim.setTaskCode("meridian-route-lung");
        claim.setPeriodKey("lifetime");
        claim.setResultCode("ROUTE_COMPLETE");
        gameStateService.applyEvent(99001L, "qa-meridian-legacy-claim", claim);

        assertThat(jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM reward_ledger WHERE user_id = 99001 AND task_code = 'meridian-route-lung'", Integer.class))
                .isEqualTo(2);
    }

    @Test
    void meridianRouteResetClearsServerProgress() {
        prepareForMeridian();
        GameProgressEventCommandDTO progress = new GameProgressEventCommandDTO();
        progress.setGameCode("meridian-river");
        progress.setEventType("TASK_PROGRESS");
        progress.setTaskCode("meridian-route-lung");
        progress.setPeriodKey("lifetime");
        progress.setResultCode("ROUTE_POINT_COMPLETE");
        gameStateService.applyEvent(99001L, "qa-meridian-reset-point", progress);

        GameProgressEventCommandDTO reset = new GameProgressEventCommandDTO();
        reset.setGameCode("meridian-river");
        reset.setEventType("TASK_RESET");
        reset.setTaskCode("meridian-route-lung");
        reset.setPeriodKey("lifetime");
        reset.setResultCode("ROUTE_RESET");
        gameStateService.applyEvent(99001L, "qa-meridian-reset", reset);

        assertThat(jdbcTemplate.queryForObject(
                "SELECT progress FROM user_task_progress WHERE user_id = 99001 AND task_code = 'meridian-route-lung'", Integer.class))
                .isZero();
        assertThat(jdbcTemplate.queryForObject(
                "SELECT status FROM user_task_progress WHERE user_id = 99001 AND task_code = 'meridian-route-lung'", String.class))
                .isEqualTo("IN_PROGRESS");
    }

    @Test
    void meridianAggregateRewardIsGrantedOnlyAfterAllRoutesAreClaimed() {
        prepareForMeridian();
        String[] slugs = {
                "lung", "large-intestine", "stomach", "spleen", "heart", "small-intestine",
                "bladder", "kidney", "pericardium", "sanjiao", "gallbladder", "liver", "ren", "du"
        };
        int[] targets = {5, 4, 5, 5, 4, 4, 5, 4, 4, 5, 6, 4, 5, 5};
        for (int route = 0; route < slugs.length; route++) {
            String taskCode = "meridian-route-" + slugs[route];
            for (int point = 1; point <= targets[route]; point++) {
                GameProgressEventCommandDTO progress = new GameProgressEventCommandDTO();
                progress.setGameCode("meridian-river");
                progress.setEventType("TASK_PROGRESS");
                progress.setTaskCode(taskCode);
                progress.setPeriodKey("lifetime");
                progress.setResultCode("ROUTE_POINT_COMPLETE");
                progress.setProgressDelta(1);
                gameStateService.applyEvent(99001L, "qa-all-routes-" + route + "-point-" + point, progress);
            }
            GameProgressEventCommandDTO claim = new GameProgressEventCommandDTO();
            claim.setGameCode("meridian-river");
            claim.setEventType("TASK_COMPLETED");
            claim.setTaskCode(taskCode);
            claim.setPeriodKey("lifetime");
            claim.setResultCode("ROUTE_COMPLETE");
            gameStateService.applyEvent(99001L, "qa-all-routes-" + route + "-claim", claim);
        }

        assertThat(jdbcTemplate.queryForObject(
                "SELECT amount FROM user_material_balance WHERE user_id = 99001 AND material_code = 'meridian-star-sand'", Integer.class))
                .isEqualTo(19);
        assertThat(jdbcTemplate.queryForObject(
                "SELECT amount FROM user_material_balance WHERE user_id = 99001 AND material_code = 'copper-token'", Integer.class))
                .isEqualTo(16);
    }

    @Test
    void weeklyChoiceRequiresFiveServerLearningCompletionsAndGrantsOnlyOnce() {
        LocalDate day = LocalDate.of(2026, 8, 27);
        for (int index = 1; index <= 5; index++) {
            jdbcTemplate.update("""
                    INSERT INTO user_task_progress
                    (user_id, game_code, task_code, period_key, progress, target, status, source, completed_at)
                    VALUES (99001, 'weekly-test', ?, ?, 1, 1, 'COMPLETED', 'SERVER', ?)
                    """, "weekly-learning-" + index, "2026-W35-" + index, day.atTime(10, index));
        }

        var available = gameStateService.getWeeklyChoiceReward(99001L, day);
        assertThat(available.getProgress()).isEqualTo(5);
        assertThat(available.getEligible()).isTrue();
        assertThat(available.getClaimed()).isFalse();

        var first = gameStateService.claimWeeklyChoiceReward(
                99001L, "qa-weekly-choice", "bamboo-slip-shard", day);
        var replay = gameStateService.claimWeeklyChoiceReward(
                99001L, "qa-weekly-choice", "bamboo-slip-shard", day);

        assertThat(first.getIdempotentReplay()).isFalse();
        assertThat(replay.getIdempotentReplay()).isTrue();
        assertThat(jdbcTemplate.queryForObject("""
                SELECT amount FROM user_material_balance
                WHERE user_id = 99001 AND material_code = 'bamboo-slip-shard'
                """, Integer.class)).isEqualTo(3);
        assertThat(jdbcTemplate.queryForObject("""
                SELECT COUNT(*) FROM reward_ledger
                WHERE user_id = 99001 AND grant_key = 'weekly-choice:2026-08-24'
                """, Integer.class)).isEqualTo(1);

        var claimed = gameStateService.getWeeklyChoiceReward(99001L, day);
        assertThat(claimed.getClaimed()).isTrue();
        assertThat(claimed.getEligible()).isFalse();
        assertThat(claimed.getSelectedItemCode()).isEqualTo("bamboo-slip-shard");
        assertThat(gameStateService.listRewardLedger(99001L, 10))
                .anyMatch(item -> "WEEKLY_CHOICE".equals(item.getSource())
                        && "bamboo-slip-shard".equals(item.getItemCode()));

        assertThatThrownBy(() -> gameStateService.claimWeeklyChoiceReward(
                99001L, "qa-weekly-choice-second", "meridian-star-sand", day))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("已经选过");
    }

    @Test
    void weeklyChoiceDoesNotCountCheckinOrLegacyImport() {
        LocalDate day = LocalDate.of(2026, 8, 27);
        for (int index = 1; index <= 4; index++) {
            jdbcTemplate.update("""
                    INSERT INTO user_task_progress
                    (user_id, game_code, task_code, period_key, progress, target, status, source, completed_at)
                    VALUES (99001, 'weekly-test', ?, ?, 1, 1, 'COMPLETED', 'SERVER', ?)
                    """, "weekly-valid-" + index, "valid-" + index, day.atTime(11, index));
        }
        jdbcTemplate.update("""
                INSERT INTO user_task_progress
                (user_id, game_code, task_code, period_key, progress, target, status, source, completed_at)
                VALUES (99001, 'checkin', 'map.checkin', '2026-08-27', 1, 1, 'COMPLETED', 'SERVER', ?)
                """, day.atTime(8, 0));
        jdbcTemplate.update("""
                INSERT INTO user_task_progress
                (user_id, game_code, task_code, period_key, progress, target, status, source, completed_at)
                VALUES (99001, 'legacy', 'legacy-week-task', 'lifetime', 1, 1, 'COMPLETED', 'LEGACY_IMPORT', ?)
                """, day.atTime(9, 0));

        var status = gameStateService.getWeeklyChoiceReward(99001L, day);
        assertThat(status.getProgress()).isEqualTo(4);
        assertThat(status.getEligible()).isFalse();
        assertThatThrownBy(() -> gameStateService.claimWeeklyChoiceReward(
                99001L, "qa-weekly-too-early", "apricot-kernel", day))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("更多学习任务");
    }
}
