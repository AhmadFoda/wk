package com.wolterskluwer.backend.dto;

import java.time.Instant;

public record CredentialsRequest(
        Instant expiresAt
) {
}
