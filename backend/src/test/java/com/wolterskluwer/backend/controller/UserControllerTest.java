package com.wolterskluwer.backend.controller;

import com.wolterskluwer.backend.dto.UserDTO;
import com.wolterskluwer.backend.dto.OrganisationDTO;
import com.wolterskluwer.backend.service.UserService;
import com.wolterskluwer.backend.service.OrganisationService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@TestPropertySource(properties = {
        "spring.datasource.url=jdbc:h2:mem:testdb",
        "spring.jpa.hibernate.ddl-auto=create-drop"
})
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private UserService userService;

    @MockitoBean
    private OrganisationService organisationService;

    @Test
    @DisplayName("Should get user details successfully")
    void shouldGetUserDetailsSuccessfully() throws Exception {
        String subjectId = "user123";
        List<OrganisationDTO> organisations = List.of(
                new OrganisationDTO(1L, "Test Org", "BE123456789", "SAP001")
        );

        UserDTO userDTO = new UserDTO(
                1L,
                "Ahmed",
                "Hamed",
                "Ahmed@example.com",
                organisations,
                true
        );

        when(userService.me(any())).thenReturn(userDTO);

        mockMvc.perform(get("/api/v1/user/me")
                        .with(jwt().jwt(jwt -> jwt.subject(subjectId))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.userId").value(1))
                .andExpect(jsonPath("$.firstName").value("Ahmed"))
                .andExpect(jsonPath("$.lastName").value("Hamed"))
                .andExpect(jsonPath("$.email").value("Ahmed@example.com"))
                .andExpect(jsonPath("$.organisations").isArray())
                .andExpect(jsonPath("$.organisations[0].id").value(1))
                .andExpect(jsonPath("$.organisations[0].name").value("Test Org"))
                .andExpect(jsonPath("$.requireSelection").value(true));
    }

    @Test
    @DisplayName("Should get user organisations successfully")
    void shouldGetUserOrganisationsSuccessfully() throws Exception {
        String subjectId = "user123";
        List<OrganisationDTO> organisations = List.of(
                new OrganisationDTO(1L, "User Org", "BE123456789", "SAP001"),
                new OrganisationDTO(2L, "Another Org", "BE987654321", "SAP002")
        );

        when(organisationService.getUserOrganisation(subjectId)).thenReturn(organisations);

        mockMvc.perform(get("/api/v1/user/organisations")
                        .with(jwt().jwt(jwt -> jwt.subject(subjectId))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].name").value("User Org"))
                .andExpect(jsonPath("$[0].vatNumber").value("BE123456789"))
                .andExpect(jsonPath("$[1].id").value(2))
                .andExpect(jsonPath("$[1].name").value("Another Org"));
    }

    @Test
    @DisplayName("Should get user with no organisations")
    void shouldGetUserWithNoOrganisations() throws Exception {
        String subjectId = "user123";
        UserDTO userDTO = new UserDTO(
                1L,
                "Ahmed",
                "Hamed",
                "Ahmed@example.com",
                List.of(),
                false
        );

        when(userService.me(any())).thenReturn(userDTO);

        mockMvc.perform(get("/api/v1/user/me")
                        .with(jwt().jwt(jwt -> jwt.subject(subjectId))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.userId").value(1))
                .andExpect(jsonPath("$.firstName").value("Ahmed"))
                .andExpect(jsonPath("$.lastName").value("Hamed"))
                .andExpect(jsonPath("$.email").value("Ahmed@example.com"))
                .andExpect(jsonPath("$.organisations").isArray())
                .andExpect(jsonPath("$.organisations").isEmpty())
                .andExpect(jsonPath("$.requireSelection").value(false));
    }

    @Test
    @DisplayName("Should get empty organisations list")
    void shouldGetEmptyOrganisationsList() throws Exception {
        String subjectId = "user123";
        when(organisationService.getUserOrganisation(subjectId)).thenReturn(List.of());

        mockMvc.perform(get("/api/v1/user/organisations")
                        .with(jwt().jwt(jwt -> jwt.subject(subjectId))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$").isEmpty());
    }

    @Test
    @DisplayName("Should return 401 when no authentication provided for /me")
    void shouldReturnUnauthorizedWhenNoAuthForMe() throws Exception {
        mockMvc.perform(get("/api/v1/user/me"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("Should return 401 when no authentication provided for /organisations")
    void shouldReturnUnauthorizedWhenNoAuthForOrganisations() throws Exception {
        mockMvc.perform(get("/api/v1/user/organisations"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("Should handle new user creation")
    void shouldHandleNewUserCreation() throws Exception {
        String subjectId = "newuser123";
        UserDTO newUserDTO = new UserDTO(
                1L,
                "New",
                "User",
                "newuser@example.com",
                List.of(),
                false
        );

        when(userService.me(any())).thenReturn(newUserDTO);

        mockMvc.perform(get("/api/v1/user/me")
                        .with(jwt().jwt(jwt -> jwt.subject(subjectId))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.userId").value(1))
                .andExpect(jsonPath("$.firstName").value("New"))
                .andExpect(jsonPath("$.lastName").value("User"))
                .andExpect(jsonPath("$.email").value("newuser@example.com"))
                .andExpect(jsonPath("$.requireSelection").value(false));
    }
}