package com.wolterskluwer.backend.service;

import com.wolterskluwer.backend.model.User;

import java.time.Instant;

public interface CredentialsValidationService {
    User validateUserAccess(String subjectId, long organisationId);

    boolean isExpirationDateValid(Instant expirationDate);

}
