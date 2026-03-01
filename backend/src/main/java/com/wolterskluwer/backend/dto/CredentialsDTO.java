package com.wolterskluwer.backend.dto;

public record CredentialsDTO (
        long id,
        String clientId,
        String creationDate,
        String expirationDate
) {}

