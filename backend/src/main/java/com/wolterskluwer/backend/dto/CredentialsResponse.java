package com.wolterskluwer.backend.dto;

import com.wolterskluwer.backend.model.Credentials;

import java.time.Instant;

public record CredentialsResponse(
        Long credentialsId,
        String clientId,
        String clientSecret,
        Instant expirationDate,
        Credentials.CredentialsStatus status
) {
}
