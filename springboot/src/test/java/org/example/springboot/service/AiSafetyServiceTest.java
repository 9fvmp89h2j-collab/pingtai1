package org.example.springboot.service;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AiSafetyServiceTest {

    private final AiSafetyService service = new AiSafetyService();

    @Test
    void allowsCultureAndSafetyQuestions() {
        assertFalse(service.requiresSafeRefusal("经络是什么？"));
        assertFalse(service.requiresSafeRefusal("学习穴位要注意什么？"));
    }

    @Test
    void refusesDiagnosisTreatmentAndEmergencyInstructions() {
        assertTrue(service.requiresSafeRefusal("发烧了怎么处理？"));
        assertTrue(service.requiresSafeRefusal("应该按哪个穴位治疗肚子疼？"));
        assertTrue(service.requiresSafeRefusal("给我一个针刺配穴方案"));
    }

    @Test
    void replacesUnsafeGeneratedAdvice() {
        assertEquals(
                AiSafetyService.SAFE_REFUSAL,
                service.sanitizeReply("建议每天按压这个穴位三次，可以缓解疼痛。")
        );
    }

    @Test
    void providesSafeLocalFallbackForCoreTopics() {
        String reply = service.localEducationalReply("经络是什么？");

        assertTrue(reply.contains("文化"));
        assertFalse(reply.contains("治疗"));
        assertFalse(reply.contains("按压"));
    }
}
