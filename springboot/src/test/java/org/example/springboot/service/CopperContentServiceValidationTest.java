package org.example.springboot.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class CopperContentServiceValidationTest {
    private final CopperContentService service = new CopperContentService();

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(service, "objectMapper", new ObjectMapper().findAndRegisterModules());
    }

    @Test
    void safeChildAcupointGetsPermanentSafetyReminder() {
        Map<String, Object> point = point("转动3D小铜人，寻找中府星。只在模型上观察。",
                "中府是文化地图上的一颗星。它属于手太阴肺经。");

        assertThat(validate("validateAcupoint", point)).isEmpty();
        assertThat(point.get("safetyTip")).isEqualTo("只看3D铜人和文化图卡，不做身体操作；有问题请告诉老师或家长。");
    }

    @Test
    void treatmentAndBodyOperationCopyIsBlocked() {
        Map<String, Object> point = point("请在自己身上按揉穴位。", "这样可以治疗不舒服。");

        assertThat(validate("validateAcupoint", point))
                .contains("儿童文案不得包含治疗承诺或身体操作指导");
    }

    @Test
    void modeledPointRequiresReviewedTraditionalUseAndSource() {
        Map<String, Object> point = point("转动3D小铜人，寻找中府星。只在模型上观察。",
                "中府是文化地图上的一颗星。它属于手太阴肺经。");
        point.put("positionX", 1);
        point.put("positionY", 2);
        point.put("positionZ", 3);

        assertThat(validate("validateAcupoint", point))
                .contains("儿童传统用途不能为空", "传统用途来源不能为空");

        point.put("childTraditionalUse", "传统认识中，这颗穴位常与呼吸和胸部舒适等身体话题相关。");
        point.put("traditionalUseSourceName", "经络腧穴学课程资料");
        assertThat(validate("validateAcupoint", point)).isEmpty();
    }

    @Test
    void completeDetectiveStoryPassesWhileMissingSafetyIsBlocked() {
        Map<String, Object> story = new LinkedHashMap<>();
        story.put("storyCode", "safe-story");
        story.put("title", "安全故事");
        story.put("summary", "观察线索并完成安全判断。");
        story.put("pages", List.of(Map.of("id", "event"), Map.of("id", "clues"), Map.of("id", "answer")));
        story.put("clues", List.of(Map.of("id", "one"), Map.of("id", "two")));
        story.put("reasoning", Map.of("prompt", "发生了什么？"));
        story.put("safety", Map.of("prompt", "怎样安全学习？"));
        story.put("rewards", List.of(Map.of("code", "copper-token", "amount", 1)));

        assertThat(validate("validateStory", story)).isEmpty();
        story.put("safety", Map.of());
        assertThat(validate("validateStory", story)).contains("安全回顾不能为空");
    }

    private Map<String, Object> point(String location, String description) {
        Map<String, Object> point = new LinkedHashMap<>();
        point.put("code", "LU-1");
        point.put("name", "中府");
        point.put("meridianCode", "LU");
        point.put("meridianName", "手太阴肺经");
        point.put("bodyArea", "胸部");
        point.put("childLocation", location);
        point.put("childDescription", description);
        point.put("sourceName", "GB/T 12346-2021");
        return point;
    }

    @SuppressWarnings("unchecked")
    private List<String> validate(String method, Map<String, Object> payload) {
        List<String> result = ReflectionTestUtils.invokeMethod(service, method, payload);
        return result == null ? new ArrayList<>() : result;
    }
}
