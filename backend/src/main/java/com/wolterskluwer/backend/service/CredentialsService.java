package com.wolterskluwer.backend.service;

import com.wolterskluwer.backend.dto.CredentialsResponse;
import jakarta.annotation.Nullable;

import java.time.Instant;

public interface CredentialsService {
    CredentialsResponse createCredentials(String subjectId, long organisationId,@Nullable Instant expiresAt);

    CredentialsResponse getCredentials(String subjectId, long organisationId);

    CredentialsResponse updateCredentials(String subjectId, long organisationId,@Nullable Instant expiresAt);

    void deleteCredentials(String subjectId, long organisationId);
}
