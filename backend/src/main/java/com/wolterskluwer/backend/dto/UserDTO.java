package com.wolterskluwer.backend.dto;

import java.util.List;


public record UserDTO(
        long userId,
        String firstName,
        String lastName,
        String email,
        List<OrganisationDTO> organisations,
        boolean requireSelection
) {
}
