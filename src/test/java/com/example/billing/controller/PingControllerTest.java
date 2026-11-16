package com.example.billing.controller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import com.example.billing.config.SecurityConfig;

@WebMvcTest(PingController.class)
@Import(SecurityConfig.class)
@TestPropertySource(properties = {
    "app.security.jwt.secret=test-secret-test-secret-test-secret-123",
    "app.security.jwt.access-ttl=15m",
    "app.security.jwt.refresh-ttl=7d"
})
class PingControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void pingIsPublic() throws Exception {
        mockMvc.perform(get("/api/v1/ping"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.status").value("ok"));
    }
}
