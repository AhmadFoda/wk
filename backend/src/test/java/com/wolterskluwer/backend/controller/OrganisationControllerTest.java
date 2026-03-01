package com.wolterskluwer.backend.controller;

import com.wolterskluwer.backend.dto.OrganisationDTO;
import com.wolterskluwer.backend.service.OrganisationService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@TestPropertySource(properties = {
        "spring.datasource.url=jdbc:h2:mem:testdb",
        "spring.jpa.hibernate.ddl-auto=create-drop"
})
class OrganisationControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private OrganisationService organisationService;

    @Test
    @DisplayName("Should get all organisations successfully")
    void shouldGetAllOrganisationsSuccessfully() throws Exception {
        List<OrganisationDTO> organisations = List.of(
                new OrganisationDTO(1L, "Org1", "BE123456789", "SAP001"),
                new OrganisationDTO(2L, "Org2", "BE987654321", "SAP002")
        );

        when(organisationService.getAllOrganizations()).thenReturn(organisations);

        mockMvc.perform(get("/api/v1/organisations")
                        .with(jwt().jwt(jwt -> jwt.subject("user123"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].name").value("Org1"))
                .andExpect(jsonPath("$[0].vatNumber").value("BE123456789"))
                .andExpect(jsonPath("$[1].id").value(2))
                .andExpect(jsonPath("$[1].name").value("Org2"));
    }

    @Test
    @DisplayName("Should get user organisations successfully")
    void shouldGetUserOrganisationsSuccessfully() throws Exception {
        List<OrganisationDTO> organisations = List.of(
                new OrganisationDTO(1L, "Test Org", "BE123456789", "SAP001")
        );

        when(organisationService.getUserOrganisation("user123")).thenReturn(organisations);

        mockMvc.perform(get("/api/v1/user/organisations")
                        .with(jwt().jwt(jwt -> jwt.subject("user123"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].name").value("Test Org"))
                .andExpect(jsonPath("$[0].vatNumber").value("BE123456789"))
                .andExpect(jsonPath("$[0].sapId").value("SAP001"));
    }

    @Test
    @DisplayName("Should assign organisations successfully")
    void shouldAssignOrganisationsSuccessfully() throws Exception {
        List<Long> orgIds = List.of(1L, 2L);

        doNothing().when(organisationService).assignUserToOrganisations("user123", orgIds);

        mockMvc.perform(post("/api/v1/organisations/assign")
                        .with(jwt().jwt(jwt -> jwt.subject("user123")))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"organisationIds\":[1,2]}"))
                .andExpect(status().isOk());

        verify(organisationService).assignUserToOrganisations("user123", orgIds);
    }

    @Test
    @DisplayName("Should remove organisation successfully")
    void shouldRemoveOrganisationSuccessfully() throws Exception {
        long orgId = 1L;

        doNothing().when(organisationService).removeUserFromOrganisation("user123", orgId);

        mockMvc.perform(delete("/api/v1/organisations/{organisationId}", orgId)
                        .with(jwt().jwt(jwt -> jwt.subject("user123"))))
                .andExpect(status().isOk());

        verify(organisationService).removeUserFromOrganisation("user123", orgId);
    }

    @Test
    @DisplayName("Should return 401 when no authentication provided")
    void shouldReturnUnauthorizedWhenNoAuth() throws Exception {
        mockMvc.perform(get("/api/v1/organisations"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("Should return 401 when no auth for assign")
    void shouldReturnUnauthorizedWhenNoAuthForAssign() throws Exception {
        mockMvc.perform(post("/api/v1/user/organisations/assign")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("[1,2]"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("Should return 401 when no auth for remove")
    void shouldReturnUnauthorizedWhenNoAuthForRemove() throws Exception {
        mockMvc.perform(delete("/api/v1/user/organisations/{organisationId}", 1L))
                .andExpect(status().isUnauthorized());
    }
}