package org.example.springboot.service;

import org.example.springboot.dto.command.MainlineConfigDraftCommandDTO;
import org.example.springboot.dto.response.MainlineLevelConfigDTO;
import org.example.springboot.dto.response.MainlineRewardConfigDTO;
import org.example.springboot.dto.response.MainlineTaskConfigDTO;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class MainlineConfigServiceValidationTest {
    private static final List<String> IDS = List.of(
            "checkin", "safe-start", "bamboo", "body", "meridian", "archive", "secret-room", "agency");
    private static final List<String> TASKS = List.of(
            "map.checkin", "main-safety-case", "main-mist-in-xinglin", "main-hand-star-map",
            "meridian-river-completion", "copper-man-daily-case", "review-daily", "main-repair-agency");
    private static final List<String> ROUTES = List.of(
            "/checkin", "/safety", "/doctor-story", "/body-map", "/jingluo", "/copper-man", "/review", "/agency");
    private static final List<String> X = List.of("13.6", "33.2", "39", "8.8", "29", "57.5", "59.5", "14.8");
    private static final List<String> Y = List.of("9.5", "11.8", "35", "40", "60", "47", "70", "74");

    private final MainlineConfigService service = new MainlineConfigService();

    @Test
    void acceptsTheFixedEightLevelBaseline() {
        var result = service.validate(command(baselineLevels()));

        assertThat(result.isValid()).withFailMessage("%s", result.getErrors()).isTrue();
        assertThat(result.getErrors()).isEmpty();
    }

    @Test
    void rejectsIllegalRouteAndRewardWithoutThrowing() {
        List<MainlineLevelConfigDTO> levels = baselineLevels();
        levels.get(2).setRoute("/not-allowed");
        levels.get(2).getTasks().get(0).setRewards(List.of(
                new MainlineRewardConfigDTO("MATERIAL", "not-registered", 1, 0)));

        var result = service.validate(command(levels));

        assertThat(result.isValid()).isFalse();
        assertThat(result.getErrors()).anyMatch(error -> error.contains("路由不可修改"));
        assertThat(result.getErrors()).anyMatch(error -> error.contains("奖励材料未注册"));
    }

    @Test
    void rejectsPrerequisiteCycle() {
        List<MainlineLevelConfigDTO> levels = baselineLevels();
        levels.get(0).setPrerequisiteLevelIds(new ArrayList<>(List.of("agency")));

        var result = service.validate(command(levels));

        assertThat(result.isValid()).isFalse();
        assertThat(result.getErrors()).anyMatch(error -> error.contains("前置关系存在循环"));
    }

    private MainlineConfigDraftCommandDTO command(List<MainlineLevelConfigDTO> levels) {
        MainlineConfigDraftCommandDTO command = new MainlineConfigDraftCommandDTO();
        command.setLevels(levels);
        return command;
    }

    private List<MainlineLevelConfigDTO> baselineLevels() {
        List<MainlineLevelConfigDTO> levels = new ArrayList<>();
        for (int index = 0; index < IDS.size(); index++) {
            MainlineLevelConfigDTO level = new MainlineLevelConfigDTO();
            level.setId(IDS.get(index));
            level.setOrder(index + 1);
            level.setLabel("关卡" + (index + 1));
            level.setStatusText("状态" + (index + 1));
            level.setDescription("关卡说明" + (index + 1));
            level.setIcon(String.valueOf(index + 1));
            level.setSeal(index == 0 ? "今" : "锁");
            level.setX(new BigDecimal(X.get(index)));
            level.setY(new BigDecimal(Y.get(index)));
            level.setRoute(ROUTES.get(index));
            level.setMapKey(IDS.get(index));
            level.setRequiredTaskIds(new ArrayList<>(List.of(TASKS.get(index))));
            level.setPrerequisiteLevelIds(index == 0 ? new ArrayList<>() : new ArrayList<>(List.of(IDS.get(index - 1))));

            MainlineTaskConfigDTO task = new MainlineTaskConfigDTO();
            task.setTaskCode(TASKS.get(index));
            task.setLevelId(level.getId());
            task.setName("任务" + (index + 1));
            task.setDescription("任务说明" + (index + 1));
            task.setRoute(level.getRoute());
            task.setTaskType(index == 0 ? "checkin" : switch (index) {
                case 1 -> "safety";
                case 2 -> "story";
                case 3 -> "acupoint";
                case 4 -> "meridian";
                case 5 -> "copper-man";
                case 6 -> "review";
                default -> "agency";
            });
            task.setEditable(index != 0);
            task.setTarget(index == 4 ? 14 : 1);
            task.setRewards(new ArrayList<>(List.of(
                    new MainlineRewardConfigDTO("MATERIAL", "copper-token", 1, 0))));
            level.setTasks(new ArrayList<>(List.of(task)));
            levels.add(level);
        }
        return levels;
    }
}
