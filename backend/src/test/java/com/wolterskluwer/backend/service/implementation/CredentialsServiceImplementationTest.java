package com.wolterskluwer.backend.service.implementation;

import com.wolterskluwer.backend.dto.CredentialsResponse;
import com.wolterskluwer.backend.exception.ForbiddenOperationException;
import com.wolterskluwer.backend.model.Credentials;
import com.wolterskluwer.backend.model.Organisation;
import com.wolterskluwer.backend.model.User;
import com.wolterskluwer.backend.repository.CredentialsRepository;
import com.wolterskluwer.backend.repository.OrganisationsRepository;
import com.wolterskluwer.backend.service.CredentialsValidationService;
import com.wolterskluwer.backend.service.SecretGenerationService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Optional;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CredentialsServiceImplementationTest {

    @Mock
    private CredentialsRepository credentialsRepository;

    @Mock
    private OrganisationsRepository organisationsRepository;

    @Mock
    private CredentialsValidationService validationService;

    @Mock
    private SecretGenerationService secretService;

    @InjectMocks
    private CredentialsServiceImplementation service;

    @Test
    @DisplayName("Should create credentials when user is member of organisation")
    void shouldCreateCredentialsSuccessfully() {
        String subjectId = "user123";
        long orgId = 1L;
        Instant expiresAt = Instant.now().plus(30, ChronoUnit.DAYS);

        User user = new User(subjectId, "Ahmed", "Hamed", "ahmed@example.com", "client123");
        Organisation organisation = new Organisation();
        organisation.setName("Test Org");
        

        when(validationService.validateUserAccess(subjectId, orgId)).thenReturn(user);
        when(validationService.isExpirationDateValid(expiresAt)).thenReturn(true);
        when(secretService.generateSecret()).thenReturn("generated-secret");
        when(credentialsRepository.existsActiveByClientIdAndOrganisationId("client123", orgId)).thenReturn(false);
        when(organisationsRepository.findById(orgId)).thenReturn(Optional.of(organisation));

        Credentials savedCredentials = new Credentials("client123", "generated-secret", Instant.now(), expiresAt, Credentials.CredentialsStatus.ACTIVE);
        savedCredentials.setOrganization(organisation);
        when(credentialsRepository.save(any(Credentials.class))).thenReturn(savedCredentials);

        CredentialsResponse result = service.createCredentials(subjectId, orgId, expiresAt);

        assertThat(result).isNotNull();
        assertThat(result.clientId()).isEqualTo("client123");
        assertThat(result.clientSecret()).isEqualTo("generated-secret");
        verify(credentialsRepository).save(any(Credentials.class));
    }

    @Test
    @DisplayName("Should throw exception when user not member of organisation")
    void shouldThrowExceptionWhenUserNotMember() {
        String subjectId = "user123";
        long orgId = 1L;

        when(validationService.validateUserAccess(subjectId, orgId))
                .thenThrow(new ForbiddenOperationException("User is not member of organisation"));

        assertThatThrownBy(() -> service.createCredentials(subjectId, orgId, Instant.now()))
                .isInstanceOf(ForbiddenOperationException.class)
                .hasMessage("User is not member of organisation");
    }

    @Test
    @DisplayName("Should throw exception when credentials already exist")
    void shouldThrowExceptionWhenCredentialsExist() {
        String subjectId = "user123";
        long orgId = 1L;
        Instant expiresAt = Instant.now().plus(30, ChronoUnit.DAYS);

        User user = new User(subjectId, "Ahmed", "Hamed", "ahmed@example.com", "client123");

        when(validationService.validateUserAccess(subjectId, orgId)).thenReturn(user);
        when(validationService.isExpirationDateValid(expiresAt)).thenReturn(true);
        when(credentialsRepository.existsActiveByClientIdAndOrganisationId("client123", orgId)).thenReturn(true);

        assertThatThrownBy(() -> service.createCredentials(subjectId, orgId, expiresAt))
                .isInstanceOf(ForbiddenOperationException.class)
                .hasMessage("Credentials already exist.");
    }
}