package com.wolterskluwer.backend.service.implementation;

import com.wolterskluwer.backend.dto.OrganisationDTO;
import com.wolterskluwer.backend.mapper.OrganisationMapper;
import com.wolterskluwer.backend.model.Organisation;
import com.wolterskluwer.backend.model.User;
import com.wolterskluwer.backend.model.UserOrganisationRelation;
import com.wolterskluwer.backend.repository.OrganisationsRepository;
import com.wolterskluwer.backend.repository.UserOrganisationRelationRepository;
import com.wolterskluwer.backend.repository.UserRepository;
import com.wolterskluwer.backend.service.OrganisationService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class OrganisationServiceImplementation implements OrganisationService {

    private final OrganisationsRepository organisationsRepository;
    private final UserOrganisationRelationRepository userOrganisationRelationRepository;
    private final UserRepository userRepository;

    @Override
    public List<OrganisationDTO> getAllOrganizations() {
        return organisationsRepository.findAll().stream().map(OrganisationMapper::toDto).toList();
    }

    @Override
    public List<OrganisationDTO> getUserOrganisation(String subjectId) {
        User user = userRepository.findWithOrganisations(subjectId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        return user.getUserOrganisationRelations().stream()
                .map(UserOrganisationRelation::getOrganization)
                .map(OrganisationMapper::toDto)
                .toList();
    }


    @Override
    @Transactional
    public void assignUserToOrganisations(String subject, List<Long> ids) {
        User user = userRepository.findWithOrganisations(subject)
                .orElseThrow(() -> new RuntimeException("User not found"));

        List<Organisation> organisations = organisationsRepository.findAllById(ids);
        if (organisations.size() != ids.size()) {
            throw new IllegalArgumentException("Invalid organisation ids");
        }

        for (Organisation organization : organisations) {
            user.addOrganisation(organization);
        }
        userRepository.save(user);
    }

    @Override
    @Transactional
    public void removeUserFromOrganisation(String subjectId, long organisationId) {
        userOrganisationRelationRepository.deleteByUser_SubjectIdAndOrganisation_Id(subjectId, organisationId);
    }

}
