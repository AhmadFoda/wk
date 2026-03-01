package com.wolterskluwer.backend.mapper;

import com.wolterskluwer.backend.dto.OrganisationDTO;
import com.wolterskluwer.backend.model.Organisation;


public class OrganisationMapper {
    private OrganisationMapper() {
    }

    public static OrganisationDTO toDto(Organisation organisation) {
        return new OrganisationDTO(organisation.getId(), organisation.getName(), organisation.getVatNumber(), organisation.getSapId());
    }
}
