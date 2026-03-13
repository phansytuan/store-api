package com.interview.demo.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.interview.demo.dto.request.LoginRequest;
import com.interview.demo.dto.request.RegisterRequest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Integration Test – AuthController
 *
 * @SpringBootTest: load full Spring context
 * @AutoConfigureMockMvc: tạo MockMvc để test HTTP layer
 *
 * Khác với Unit Test: không mock, test end-to-end với PostgreSQL DB thật (docker-compose)
 * 
 * NOTE: Testcontainers is not used because Testcontainers 1.21.3 bundles a shaded
 * docker-java (API v1.32) incompatible with Docker 29.x (minimum API 1.40).
 * The test relies on the running docker-compose postgres (localhost:5432/storedb).
 */
@SpringBootTest
@AutoConfigureMockMvc
@DisplayName("AuthController Integration Tests")
class AuthControllerTest {

    @Autowired private MockMvc       mockMvc;
    @Autowired private ObjectMapper  objectMapper;

    @Test
    @DisplayName("POST /auth/register - success")
    void register_withValidData_shouldReturn201() throws Exception {
        RegisterRequest request = new RegisterRequest();
        request.setFullName("Test User");
        request.setEmail("test" + System.currentTimeMillis() + "@test.com");
        request.setPassword("password123");

        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.token").isNotEmpty())
                .andExpect(jsonPath("$.data.tokenType").value("Bearer"));
    }

    @Test
    @DisplayName("POST /auth/login - with seeded admin user")
    void login_withValidCredentials_shouldReturnToken() throws Exception {
        LoginRequest request = new LoginRequest();
        request.setEmail("admin@demo.com");
        request.setPassword("admin123");

        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.token").isNotEmpty());
    }

    @Test
    @DisplayName("POST /auth/login - invalid credentials → 401")
    void login_withWrongPassword_shouldReturn401() throws Exception {
        LoginRequest request = new LoginRequest();
        request.setEmail("admin@demo.com");
        request.setPassword("wrongpassword");

        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.errorCode").value("INVALID_CREDENTIALS"));
    }

    @Test
    @DisplayName("POST /auth/register - invalid email → 400 Validation")
    void register_withInvalidEmail_shouldReturn400() throws Exception {
        RegisterRequest request = new RegisterRequest();
        request.setFullName("Test");
        request.setEmail("not-an-email");   // invalid
        request.setPassword("pass123");

        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errorCode").value("VALIDATION_FAILED"));
    }
}
