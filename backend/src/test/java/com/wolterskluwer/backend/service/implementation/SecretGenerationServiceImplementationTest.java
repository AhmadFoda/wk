package com.wolterskluwer.backend.service.implementation;


import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
class SecretGenerationServiceImplementationTest {

    @InjectMocks
    private SecretGenerationServiceImplementation secretService;

    @Test
    @DisplayName("Should generate secret with correct length")
    void shouldGenerateSecretWithCorrectLength() {
        String secret = secretService.generateSecret();
        assertThat(secret).isNotEmpty();
        assertThat(secret.length()).isGreaterThan(20);
    }

    @Test
    @DisplayName("Should generate different secrets each time")
    void shouldGenerateDifferentSecretsEachTime() {
        String secret1 = secretService.generateSecret();
        String secret2 = secretService.generateSecret();
        assertThat(secret1).isNotEqualTo(secret2);
    }
}
