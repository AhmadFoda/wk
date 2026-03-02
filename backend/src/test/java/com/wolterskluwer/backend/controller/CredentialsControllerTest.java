package com.wolterskluwer.backend.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.wolterskluwer.backend.dto.CredentialsResponse;
import com.wolterskluwer.backend.exception.ForbiddenOperationException;
import com.wolterskluwer.backend.model.Credentials;
import com.wolterskluwer.backend.security.EncryptionService;
import com.wolterskluwer.backend.service.CredentialsService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@TestPropertySource(properties = {
        "spring.datasource.url=jdbc:h2:mem:testdb",
        "spring.jpa.hibernate.ddl-auto=create-drop"
})
class CredentialsControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private CredentialsService credentialsService;

    @MockitoBean
    EncryptionService encryptionService;

    @Test
    @DisplayName("Should create credentials successfully")
    void shouldCreateCredentialsSuccessfully() throws Exception {

        Long orgId = 1L;
        Instant expiresAt = Instant.now().plus(30, ChronoUnit.DAYS);

        CredentialsResponse response = new CredentialsResponse(
                1L,
                "client123",
                "generated-secret",
                expiresAt,
                Credentials.CredentialsStatus.ACTIVE
        );

        when(credentialsService.createCredentials(anyString(), eq(orgId), any(Instant.class)))
                .thenReturn(response);

        mockMvc.perform(post("/api/v1/credentials/organisations/{orgId}", orgId)
                        .with(jwt().jwt(jwt -> jwt.subject("user123")))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"expiresAt\":\"" + expiresAt + "\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.clientId").value("client123"))
                .andExpect(jsonPath("$.clientSecret").value("generated-secret"))
                .andExpect(jsonPath("$.status").value("ACTIVE"));
    }

    @Test
    @DisplayName("Should get credentials successfully")
    void shouldGetCredentialsSuccessfully() throws Exception {
        Long orgId = 1L;
        Instant expiresAt = Instant.now().plus(30, ChronoUnit.DAYS);

        CredentialsResponse response = new CredentialsResponse(
                1L,
                "client123",
                "***",
                expiresAt,
                Credentials.CredentialsStatus.ACTIVE
        );

        when(credentialsService.getCredentials(anyString(), eq(orgId)))
                .thenReturn(response);

        mockMvc.perform(get("/api/v1/credentials/organisations/{orgId}", orgId)
                        .with(jwt().jwt(jwt -> jwt.subject("user123"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.clientId").value("client123"))
                .andExpect(jsonPath("$.clientSecret").value("***"))
                .andExpect(jsonPath("$.status").value("ACTIVE"));
    }

    @Test
    @DisplayName("Should update credentials successfully")
    void shouldUpdateCredentialsSuccessfully() throws Exception {
        Long orgId = 1L;
        Instant expiresAt = Instant.now().plus(30, ChronoUnit.DAYS);

        CredentialsResponse response = new CredentialsResponse(
                1L,
                "client123",
                "updated-secret",
                expiresAt,
                Credentials.CredentialsStatus.ACTIVE
        );

        when(credentialsService.updateCredentials(anyString(), eq(orgId), any(Instant.class)))
                .thenReturn(response);

        mockMvc.perform(put("/api/v1/credentials/organisations/{orgId}", orgId)
                        .with(jwt().jwt(jwt -> jwt.subject("user123")))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"expiresAt\":\"" + expiresAt + "\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.clientId").value("client123"))
                .andExpect(jsonPath("$.clientSecret").value("updated-secret"))
                .andExpect(jsonPath("$.status").value("ACTIVE"));
    }

    @Test
    @DisplayName("Should delete credentials successfully")
    void shouldDeleteCredentialsSuccessfully() throws Exception {
        Long orgId = 1L;

        org.mockito.Mockito.doNothing()
                .when(credentialsService)
                .deleteCredentials(anyString(), eq(orgId));

        mockMvc.perform(delete("/api/v1/credentials/organisations/{orgId}", orgId)
                        .with(jwt().jwt(jwt -> jwt.subject("user123"))))
                .andExpect(status().isNoContent());
    }

    @Test
    @DisplayName("Should return 403 when user not authorized to create credentials")
    void shouldReturnForbiddenWhenNotAuthorizedToCreate() throws Exception {
        Long orgId = 1L;
        Instant expiresAt = Instant.now().plus(30, ChronoUnit.DAYS);

        when(credentialsService.createCredentials(anyString(), anyLong(), any(Instant.class)))
                .thenThrow(new ForbiddenOperationException("User is not member of organisation"));

        mockMvc.perform(post("/api/v1/credentials/organisations/{orgId}", orgId)
                        .with(jwt().jwt(jwt -> jwt.subject("user123")))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"expiresAt\":\"" + expiresAt + "\"}"))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("Should return 403 when user not authorized to get credentials")
    void shouldReturnForbiddenWhenNotAuthorizedToGet() throws Exception {
        Long orgId = 1L;

        when(credentialsService.getCredentials(anyString(), anyLong()))
                .thenThrow(new ForbiddenOperationException("Credentials not found"));

        mockMvc.perform(get("/api/v1/credentials/organisations/{orgId}", orgId)
                        .with(jwt().jwt(jwt -> jwt.subject("user123"))))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("Should return 401 when no authentication provided")
    void shouldReturnUnauthorizedWhenNoAuth() throws Exception {
        Long orgId = 1L;

        mockMvc.perform(post("/api/v1/credentials/organisations/{orgId}", orgId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"expiresAt\":\"2026-03-31T12:00:00Z\"}"))
                .andExpect(status().isUnauthorized());
    }
}