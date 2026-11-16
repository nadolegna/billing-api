package com.example.billing.controller;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import com.example.billing.config.SecurityConfig;
import com.example.billing.service.UserService;

@WebMvcTest(UserController.class)
@Import(SecurityConfig.class)
@TestPropertySource(properties = {
    "app.security.jwt.secret=test-secret-test-secret-test-secret-123",
    "app.security.jwt.access-ttl=15m",
    "app.security.jwt.refresh-ttl=7d"
})
class UserControllerSecurityTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private UserService userService;

    @Test
    void listRequiresAuthentication() throws Exception {
        mockMvc.perform(get("/api/v1/users"))
            .andExpect(status().isUnauthorized());
    }

    @Test
    void staffCannotListUsers() throws Exception {
        mockMvc.perform(get("/api/v1/users")
                .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_STAFF"))))
            .andExpect(status().isForbidden());
    }

    @Test
    void adminCanListUsers() throws Exception {
        mockMvc.perform(get("/api/v1/users")
                .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_ADMIN"))))
            .andExpect(status().isOk());
    }
}
