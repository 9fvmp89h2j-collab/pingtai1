package org.example.springboot.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class SafetyGameConfigServiceTest {
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final SafetyGameConfigService service = new SafetyGameConfigService();

    @Test
    void seededSafetyGameConfigPassesPublishValidation() throws Exception {
        assertThat(service.validateConfig(seedConfig())).isEmpty();
    }

    @Test
    void publishValidationRejectsScenarioWithoutMediaDescription() throws Exception {
        Map<String, Object> config = seedConfig();
        List<Map<String, Object>> scenarios = maps(config.get("scenarios"));
        scenarios.get(0).put("imageAlt", "");

        assertThat(service.validateConfig(config))
                .anyMatch(error -> error.contains("缺少图片或图片说明"));
    }

    private Map<String, Object> seedConfig() throws Exception {
        try (InputStream stream = getClass().getResourceAsStream("/db/migration/V10__safety_game_config.sql")) {
            if (stream == null) throw new IllegalStateException("V10 migration not found");
            String sql = new String(stream.readAllBytes(), StandardCharsets.UTF_8);
            int start = sql.indexOf("'{") + 1;
            int end = sql.lastIndexOf("', CURRENT_TIMESTAMP);");
            return objectMapper.readValue(sql.substring(start, end), new TypeReference<>() {});
        }
    }

    @SuppressWarnings("unchecked")
    private static List<Map<String, Object>> maps(Object value) {
        return (List<Map<String, Object>>) value;
    }
}
