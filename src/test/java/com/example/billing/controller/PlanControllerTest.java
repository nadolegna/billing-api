package com.example.billing.controller;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import com.example.billing.config.SecurityConfig;
import com.example.billing.exception.GlobalExceptionHandler;
import com.example.billing.service.PlanService;

@WebMvcTest(PlanController.class)
@Import({SecurityConfig.class, GlobalExceptionHandler.class})
@TestPropertySource(properties = {
    "app.security.jwt.secret=test-secret-test-secret-test-secret-123",
    "app.security.jwt.access-ttl=15m",
    "app.security.jwt.refresh-ttl=7d"
})
class PlanControllerTest {

    private static final String VALID_PLAN = """
        {"name":"Pro","price":49.00,"currency":"USD","billingInterval":"MONTHLY"}
        """;

    private static final String INVALID_PLAN = """
        {"name":"","price":-1,"currency":"usd"}
        """;

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private PlanService planService;

    @Test
    void staffCannotCreatePlans() throws Exception {
        mockMvc.perform(post("/api/v1/plans")
                .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_STAFF")))
                .contentType(MediaType.APPLICATION_JSON)
                .content(VALID_PLAN))
            .andExpect(status().isForbidden())
            .andExpect(jsonPath("$.title").value("Forbidden"));
    }

    @Test
    void invalidPlanReturnsFieldErrorsAsProblemDetail() throws Exception {
        mockMvc.perform(post("/api/v1/plans")
                .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_ADMIN")))
                .contentType(MediaType.APPLICATION_JSON)
                .content(INVALID_PLAN))
            .andExpect(status().isBadRequest())
            .andExpect(content().contentTypeCompatibleWith("application/problem+json"))
            .andExpect(jsonPath("$.title").value("Validation failed"))
            .andExpect(jsonPath("$.errors.name").exists())
            .andExpect(jsonPath("$.errors.price").exists())
            .andExpect(jsonPath("$.errors.currency").exists())
            .andExpect(jsonPath("$.errors.billingInterval").exists());
    }
}
