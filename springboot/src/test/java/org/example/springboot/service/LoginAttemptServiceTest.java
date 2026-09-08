package org.example.springboot.service;

import org.example.springboot.exception.BusinessException;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

class LoginAttemptServiceTest {

    @Test
    void blocksSixthAttemptForSameAddressAndIdentity() {
        LoginAttemptService service = new LoginAttemptService();
        for (int i = 0; i < 5; i++) {
            assertDoesNotThrow(() -> service.checkAllowed("127.0.0.1", "child"));
            service.recordFailure("127.0.0.1", "child");
        }

        assertThrows(BusinessException.class,
                () -> service.checkAllowed("127.0.0.1", "child"));
        assertDoesNotThrow(() -> service.checkAllowed("127.0.0.2", "child"));
    }

    @Test
    void successfulLoginClearsFailures() {
        LoginAttemptService service = new LoginAttemptService();
        for (int i = 0; i < 5; i++) {
            service.recordFailure("127.0.0.1", "child");
        }
        service.clear("127.0.0.1", "child");

        assertDoesNotThrow(() -> service.checkAllowed("127.0.0.1", "child"));
    }
}
