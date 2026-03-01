package com.wolterskluwer.backend.dto;

import java.util.List;

public record AssignOrganisationRequestDTO (
        List<Long> organisationIds
){
}
