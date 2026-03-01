package com.wolterskluwer.backend.dto;

import java.util.List;

public record OrganisationDetailsDTO(
        long id,
        String name,
        String vatNumber,
        String sapId,
        List<CredentialsDTO> credentials
) {}
