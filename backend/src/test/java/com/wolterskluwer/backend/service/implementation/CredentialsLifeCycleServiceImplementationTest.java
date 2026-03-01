package com.wolterskluwer.backend.service.implementation;

import com.wolterskluwer.backend.model.Credentials;
import com.wolterskluwer.backend.repository.CredentialsRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CredentialsLifeCycleServiceImplementationTest {

    @Mock
    private CredentialsRepository credentialsRepository;

    @InjectMocks
    private CredentialsLifeCycleServiceImplementation lifeCycleService;

    @Test
    @DisplayName("Should check expired credentials")
    void shouldCheckExpiredCredentials() {

        Instant past = Instant.now().minus(1, ChronoUnit.DAYS);
        Credentials expiredCredential = new Credentials("client1", "secret1", Instant.now(), past, Credentials.CredentialsStatus.ACTIVE);

        when(credentialsRepository.findByStatusAndExpirationDateBefore(
                eq(Credentials.CredentialsStatus.ACTIVE), any(Instant.class)))
                .thenReturn(List.of(expiredCredential));

        lifeCycleService.checkExpiredCredentials();
        verify(credentialsRepository).findByStatusAndExpirationDateBefore(
                eq(Credentials.CredentialsStatus.ACTIVE), any(Instant.class));
        verify(credentialsRepository).save(expiredCredential);
        assertThat(expiredCredential.getStatus()).isEqualTo(Credentials.CredentialsStatus.INACTIVE);
    }

    @Test
    @DisplayName("Should handle no expired credentials")
    void shouldHandleNoExpiredCredentials() {

        when(credentialsRepository.findByStatusAndExpirationDateBefore(
                eq(Credentials.CredentialsStatus.ACTIVE), any(Instant.class)))
                .thenReturn(List.of());


        lifeCycleService.checkExpiredCredentials();


        verify(credentialsRepository).findByStatusAndExpirationDateBefore(
                eq(Credentials.CredentialsStatus.ACTIVE), any(Instant.class));
        verify(credentialsRepository, never()).save(any(Credentials.class));
    }
}