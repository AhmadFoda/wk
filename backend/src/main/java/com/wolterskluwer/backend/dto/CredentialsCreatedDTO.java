package com.wolterskluwer.backend.dto;

public record CredentialsCreatedDTO(
        long id,
        String clientId,
        String clientSecret,
        String creationDate,
        String expirationDate
) {
}
