package org.example.springboot.config;

import org.example.springboot.service.UserService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;

@WebMvcTest(SecurityTestController.class)
@Import(SecurityConfig.class)
@ContextConfiguration(classes = { SecurityTestController.class, SecurityConfig.class })
class SecurityConfigTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private UserService userService;

    @Test
    void publicEducationalGetRemainsAvailable() throws Exception {
        mockMvc.perform(get("/api/acupuncture/copper-man/acupoints"))
            .andExpect(status().isOk())
            .andExpect(header().string("X-Security-Test", "ok"));
    }

    @Test
    void educationalWriteRequiresAuthentication() throws Exception {
        mockMvc.perform(post("/api/acupuncture/doctor-story/ping"))
            .andExpect(status().isForbidden());
    }

    @Test
    void legacyClinicalCatalogsRejectAnonymousUsers() throws Exception {
        mockMvc.perform(get("/api/acupuncture/xuewei/ping"))
            .andExpect(status().isForbidden());
        mockMvc.perform(get("/api/acupuncture/zhenjiu-tools/ping"))
            .andExpect(status().isForbidden());
    }

    @Test
    void legacyClinicalCatalogsRejectOrdinaryUsersButAllowAdministrators() throws Exception {
        mockMvc.perform(get("/api/acupuncture/xuewei/ping").with(user("child").roles("USER")))
            .andExpect(status().isForbidden());
        mockMvc.perform(get("/api/acupuncture/zhenjiu-tools/ping").with(user("admin").roles("ADMIN")))
            .andExpect(status().isOk());
        mockMvc.perform(get("/api/acupuncture/doctor-story/ping").with(user("child").roles("USER")))
            .andExpect(status().isForbidden());
    }

    @Test
    void privateAndAdminEndpointsRejectAnonymousUsers() throws Exception {
        mockMvc.perform(get("/api/private/ping"))
            .andExpect(status().isForbidden());
        mockMvc.perform(get("/api/admin/ping"))
            .andExpect(status().isForbidden());
    }

    @Test
    void loginEntryPointRemainsPublic() throws Exception {
        mockMvc.perform(post("/api/user/login"))
            .andExpect(status().isOk())
            .andExpect(header().string("X-Security-Test", "ok"));
    }

    @Test
    void missingPublicResourceUsesHttpNotFound() throws Exception {
        mockMvc.perform(get("/missing.html"))
            .andExpect(status().isNotFound());
    }
}
