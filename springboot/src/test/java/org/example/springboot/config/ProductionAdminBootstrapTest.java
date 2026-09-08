package org.example.springboot.config;

import org.example.springboot.entity.User;
import org.example.springboot.mapper.UserMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ProductionAdminBootstrapTest {

    @Mock
    private UserMapper userMapper;
    @Mock
    private PasswordEncoder passwordEncoder;

    @Test
    void createsAdminFromEnvironmentWhenNoneExists() throws Exception {
        when(userMapper.selectCount(any())).thenReturn(0L);
        when(passwordEncoder.encode("a-strong-bootstrap-password")).thenReturn("encoded");
        ProductionAdminBootstrap bootstrap = configuredBootstrap();

        bootstrap.run(null);

        verify(userMapper).insert(any(User.class));
    }

    @Test
    void leavesExistingAdministratorUntouched() throws Exception {
        when(userMapper.selectCount(any())).thenReturn(1L);
        ProductionAdminBootstrap bootstrap = configuredBootstrap();

        bootstrap.run(null);

        verify(userMapper, never()).insert(any(User.class));
    }

    private ProductionAdminBootstrap configuredBootstrap() {
        ProductionAdminBootstrap bootstrap = new ProductionAdminBootstrap(userMapper, passwordEncoder);
        ReflectionTestUtils.setField(bootstrap, "username", "site_admin");
        ReflectionTestUtils.setField(bootstrap, "email", "admin@example.com");
        ReflectionTestUtils.setField(bootstrap, "password", "a-strong-bootstrap-password");
        return bootstrap;
    }
}
