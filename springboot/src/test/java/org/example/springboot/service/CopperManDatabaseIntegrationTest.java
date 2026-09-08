package org.example.springboot.service;

import com.baomidou.mybatisplus.test.autoconfigure.MybatisPlusTest;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.Resource;
import org.example.springboot.dto.response.CopperManDailyCaseResponseDTO;
import org.example.springboot.dto.response.CopperManDiscoverResponseDTO;
import org.example.springboot.entity.AcupointKnowledge;
import org.example.springboot.entity.UserCopperManProfile;
import org.example.springboot.exception.BusinessException;
import org.example.springboot.mapper.AcupointKnowledgeMapper;
import org.example.springboot.mapper.UserCopperManProfileMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.Import;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.jdbc.Sql;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@MybatisPlusTest(properties = {
        "spring.datasource.url=jdbc:h2:mem:copperman;MODE=MySQL;DB_CLOSE_DELAY=-1;DATABASE_TO_LOWER=TRUE;NON_KEYWORDS=USER",
        "spring.datasource.driver-class-name=org.h2.Driver",
        "spring.datasource.username=sa",
        "spring.datasource.password=",
        "spring.sql.init.mode=never",
        "spring.flyway.enabled=false"
})
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import({CopperManService.class, GameStateService.class, MainlineConfigService.class, WriteOperationService.class,
        AuditEventService.class, CopperManDatabaseIntegrationTest.JsonTestConfiguration.class})
@Sql(scripts = "/schema-copper-man-h2.sql")
class CopperManDatabaseIntegrationTest {
    @TestConfiguration
    static class JsonTestConfiguration {
        @Bean
        ObjectMapper objectMapper() {
            return new ObjectMapper().findAndRegisterModules();
        }
    }

    @Resource
    private CopperManService service;
    @Resource
    private AcupointKnowledgeMapper acupointKnowledgeMapper;
    @Resource
    private UserCopperManProfileMapper profileMapper;
    @Resource
    private JdbcTemplate jdbcTemplate;
    @Resource
    private GameStateService gameStateService;

    private final LocalDate caseDate = LocalDate.of(2026, 8, 9);

    @BeforeEach
    void seedKnowledge() {
        for (int i = 1; i <= 8; i++) {
            acupointKnowledgeMapper.insert(point(i));
        }
    }

    private void prepareForCopperMan() {
        completeTask("map.checkin", 1, 1);
        completeTask("main-safety-case", 1, 1);
        completeTask("main-mist-in-xinglin", 1, 1);
        completeTask("main-hand-star-map", 10, 10);
        completeTask("meridian-river-completion", 14, 14);
    }

    private void prepareForAgency() {
        prepareForCopperMan();
        completeTask("copper-man-daily-case", 1, 1);
        completeTask("review-daily", 1, 1);
    }

    private void completeTask(String taskCode, int progress, int target) {
        jdbcTemplate.update("""
                INSERT INTO user_task_progress
                (user_id, game_code, task_code, period_key, progress, target, status, source, completed_at)
                VALUES (99001, 'test', ?, 'lifetime', ?, ?, 'COMPLETED', 'TEST', CURRENT_TIMESTAMP)
                """, taskCode, progress, target);
    }

    @Test
    void discoveryProgressAndRewardsPersistInDatabaseWithoutDuplication() {
        long userId = 99001L;
        prepareForCopperMan();
        CopperManDailyCaseResponseDTO dailyCase = service.getDailyCase(userId, caseDate);

        CopperManDiscoverResponseDTO first = service.discover(userId, dailyCase.getTargetCodes().get(0), caseDate);
        CopperManDiscoverResponseDTO second = service.discover(userId, dailyCase.getTargetCodes().get(1), caseDate);
        CopperManDiscoverResponseDTO completed = service.discover(userId, dailyCase.getTargetCodes().get(2), caseDate);
        CopperManDiscoverResponseDTO repeated = service.discover(userId, dailyCase.getTargetCodes().get(2), caseDate);
        UserCopperManProfile profile = profileMapper.selectById(userId);

        assertThat(first.getDiscoveredCodes()).hasSize(1);
        assertThat(second.getDiscoveredCodes()).hasSize(2);
        assertThat(completed.getDiscoveredCodes()).hasSize(3);
        assertThat(completed.getNewlyCompleted()).isTrue();
        assertThat(repeated.getNewlyCompleted()).isFalse();
        assertThat(profile.getCopperTokens()).isEqualTo(2);
        assertThat(profile.getStarSand()).isEqualTo(5);
        assertThat(profile.getCompletedCases()).isEqualTo(1);
        assertThat(materialAmount(userId, "copper-token")).isEqualTo(2);
        assertThat(materialAmount(userId, "meridian-star-sand")).isEqualTo(5);
        assertThat(jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM reward_ledger WHERE user_id = ? AND task_code = 'copper-man-daily-case'",
                Integer.class, userId)).isEqualTo(2);
        assertThat(service.listAcupoints(userId).stream()
                .filter(point -> Boolean.TRUE.equals(point.getDiscovered()))
                .map(point -> point.getCode()))
                .containsExactlyInAnyOrderElementsOf(dailyCase.getTargetCodes());
        assertThat(service.listAcupoints(userId))
                .allSatisfy(point -> {
                    assertThat(point.getChildTraditionalUse()).isNotBlank();
                    assertThat(point.getTraditionalUseSourceName()).isNotBlank();
                });
    }

    @Test
    void agencyExchangeDeductsMaterialsPersistsArchiveAndReplaysSafely() {
        long userId = 99001L;
        prepareForAgency();
        jdbcTemplate.update("INSERT INTO user_material_balance (user_id, material_code, amount) VALUES (?, 'bamboo-slip-shard', 3)", userId);
        jdbcTemplate.update("INSERT INTO user_material_balance (user_id, material_code, amount) VALUES (?, 'copper-token', 2)", userId);
        jdbcTemplate.update("INSERT INTO user_material_balance (user_id, material_code, amount) VALUES (?, 'meridian-star-sand', 4)", userId);

        assertThatThrownBy(() -> gameStateService.exchangeAgencyArchiveItem(
                userId, "agency-test:star-wall-before-gate", "star-wall"))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("请先完成前面的侦探社修复项目");

        gameStateService.exchangeAgencyArchiveItem(userId, "agency-test:gate", "gate");
        gameStateService.exchangeAgencyArchiveItem(userId, "agency-test:gate", "gate");

        assertThat(materialAmount(userId, "bamboo-slip-shard")).isZero();
        assertThat(materialAmount(userId, "copper-token")).isEqualTo(1);
        assertThat(jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM user_agency_archive WHERE user_id = ? AND archive_item_id = 'gate'",
                Integer.class, userId)).isEqualTo(1);
        assertThat(jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM reward_ledger WHERE user_id = ? AND source = 'AGENCY_EXCHANGE'",
                Integer.class, userId)).isEqualTo(2);
        assertThat(jdbcTemplate.queryForObject(
                "SELECT status FROM user_task_progress WHERE user_id = ? AND task_code = 'main-repair-agency'",
                String.class, userId)).isEqualTo("COMPLETED");
        assertThat(jdbcTemplate.queryForObject(
                "SELECT progress FROM user_task_progress WHERE user_id = ? AND task_code = 'main-repair-agency'",
                Integer.class, userId)).isEqualTo(1);
        assertThat(materialAmount(userId, "gold-needle-badge-shard")).isZero();
    }

    @Test
    void completingAllAgencyRepairsKeepsTheFinalMainlineTaskComplete() {
        long userId = 99001L;
        prepareForAgency();
        addMaterial(userId, "bamboo-slip-shard", 11);
        addMaterial(userId, "copper-token", 3);
        addMaterial(userId, "herbal-leaf", 5);
        addMaterial(userId, "meridian-star-sand", 8);
        addMaterial(userId, "safety-bell", 2);
        addMaterial(userId, "acupoint-star-pearl", 2);
        addMaterial(userId, "star-compass", 1);

        for (String itemId : java.util.List.of(
                "gate", "herb-cabinet", "star-wall", "bell-wall", "display", "archive", "roof")) {
            gameStateService.exchangeAgencyArchiveItem(userId, "agency-complete:" + itemId, itemId);
        }

        assertThat(jdbcTemplate.queryForObject("""
                SELECT status FROM user_task_progress
                WHERE user_id = ? AND task_code = 'main-repair-agency'
                """, String.class, userId)).isEqualTo("COMPLETED");
        assertThat(jdbcTemplate.queryForObject("""
                SELECT progress FROM user_task_progress
                WHERE user_id = ? AND task_code = 'main-repair-agency'
                """, Integer.class, userId)).isEqualTo(1);
        assertThat(jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM user_agency_archive WHERE user_id = ?",
                Integer.class, userId)).isEqualTo(7);
    }

    private void addMaterial(long userId, String materialCode, int amount) {
        jdbcTemplate.update("INSERT INTO user_material_balance (user_id, material_code, amount) VALUES (?, ?, ?)",
                userId, materialCode, amount);
    }

    private int materialAmount(long userId, String materialCode) {
        Integer amount = jdbcTemplate.queryForObject(
                "SELECT COALESCE(MAX(amount), 0) FROM user_material_balance WHERE user_id = ? AND material_code = ?",
                Integer.class, userId, materialCode);
        return amount == null ? 0 : amount;
    }

    private AcupointKnowledge point(int order) {
        AcupointKnowledge point = new AcupointKnowledge();
        point.setCode("T-" + order);
        point.setPointNumber(order);
        point.setName("测试星点" + order);
        point.setPinyin("Test" + order);
        point.setMeridianCode("T");
        point.setMeridianName("测试经络");
        point.setModelId("p" + order);
        point.setBodyArea("手腕部");
        point.setStandardLocation("测试标准定位");
        point.setChildLocation("在3D铜人上寻找发光点");
        point.setChildDescription("这是一颗帮助认识身体地图的文化星");
        point.setChildTraditionalUse("传统认识中，这颗穴位常与身体舒适等文化话题相关。");
        point.setSafetyTip("只观察和学习，不自己尝试针刺");
        point.setSourceName("测试标准");
        point.setTraditionalUseSourceName("测试课程资料");
        point.setTraditionalUseSourceLink("https://example.test/acupoint-source");
        point.setPositionX(BigDecimal.valueOf(order));
        point.setPositionY(BigDecimal.ZERO);
        point.setPositionZ(BigDecimal.ZERO);
        point.setSortOrder(order);
        point.setEnabled(true);
        return point;
    }
}
