package com.wolterskluwer.backend.service;

import com.wolterskluwer.backend.dto.OrganisationDTO;

import java.util.List;

public interface OrganisationService {

    List<OrganisationDTO> getAllOrganizations();

    List<OrganisationDTO> getUserOrganisation(String subjectId);

    void assignUserToOrganisations(String subject, List<Long> ids);     // Org assigns users

    void removeUserFromOrganisation(String subjectId, long organisationId); // Org removes users

}
