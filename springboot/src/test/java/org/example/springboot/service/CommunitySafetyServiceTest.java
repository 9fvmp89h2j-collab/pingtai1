package org.example.springboot.service;

import org.example.springboot.exception.BusinessException;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

class CommunitySafetyServiceTest {

    private final CommunitySafetyService service = new CommunitySafetyService();

    @Test
    void allowsCultureAndLearningExperience() {
        assertDoesNotThrow(() -> service.validatePost("我找到一个星点", "今天认识了足三里这个名字。"));
    }

    @Test
    void blocksTreatmentInstructions() {
        assertThrows(BusinessException.class,
                () -> service.validateComment("建议每天按压这个穴位，可以治疗肚子疼。"));
    }

    @Test
    void blocksPersonalContactDetails() {
        assertThrows(BusinessException.class,
                () -> service.validatePost("加我", "我的手机号是13800138000"));
    }
}
