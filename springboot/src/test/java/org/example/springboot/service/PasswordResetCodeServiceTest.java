package org.example.springboot.service;

import org.example.springboot.entity.User;
import org.example.springboot.exception.BusinessException;
import org.example.springboot.mapper.UserMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PasswordResetCodeServiceTest {

    @Mock
    private UserMapper userMapper;

    @Mock
    private JavaMailSender mailSender;

    @InjectMocks
    private PasswordResetCodeService service;

    @BeforeEach
    void configureSender() {
        ReflectionTestUtils.setField(service, "sender", "noreply@example.com");
    }

    @Test
    void unknownAccountDoesNotSendMailOrRevealAnError() {
        when(userMapper.selectOne(any())).thenReturn(null);

        assertDoesNotThrow(() -> service.sendCode("missing-user", "parent@example.com"));

        verify(mailSender, never()).send(any(SimpleMailMessage.class));
    }

    @Test
    void generatedCodeCanBeUsedOnlyOnce() {
        when(userMapper.selectOne(any())).thenReturn(new User());
        service.sendCode("child", "parent@example.com");

        ArgumentCaptor<SimpleMailMessage> message = ArgumentCaptor.forClass(SimpleMailMessage.class);
        verify(mailSender).send(message.capture());
        String code = message.getValue().getText().replaceAll("(?s).*验证码是：(\\d{6}).*", "$1");

        assertDoesNotThrow(() -> service.verifyAndConsume("child", "parent@example.com", code));
        assertThrows(BusinessException.class,
                () -> service.verifyAndConsume("child", "parent@example.com", code));
    }
}
