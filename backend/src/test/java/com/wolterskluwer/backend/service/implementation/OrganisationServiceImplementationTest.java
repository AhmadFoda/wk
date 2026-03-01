package com.wolterskluwer.backend.service.implementation;

import com.wolterskluwer.backend.dto.OrganisationDTO;

import com.wolterskluwer.backend.model.Organisation;
import com.wolterskluwer.backend.model.User;
import com.wolterskluwer.backend.model.UserOrganisationRelation;
import com.wolterskluwer.backend.repository.OrganisationsRepository;
import com.wolterskluwer.backend.repository.UserOrganisationRelationRepository;
import com.wolterskluwer.backend.repository.UserRepository;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class OrganisationServiceImplementationTest {

    @Mock
    private OrganisationsRepository organisationsRepository;

    @Mock
    private UserRepository userRepository;
    @Mock
    private UserOrganisationRelationRepository relationRepository;

    @InjectMocks
    private OrganisationServiceImplementation service;

    @Test
    @DisplayName("Should get user organisations successfully")
    void shouldGetUserOrganisationsSuccessfully() {
        String subjectId = "user123";

        User user = new User(subjectId, "Ahmed", "Hamed", "Ahmed@example.com", "client123");
        Organisation organisation = new Organisation();
        organisation.setName("Test Org");
        UserOrganisationRelation relation = new UserOrganisationRelation(user, organisation);

        user.getUserOrganisationRelations().add(relation);

        when(userRepository.findWithOrganisations(subjectId)).thenReturn(Optional.of(user));

        List<OrganisationDTO> result = service.getUserOrganisation(subjectId);

        assertEquals(1, result.size());
        assertThat(result.get(0).name()).isEqualTo("Test Org");
    }

    @Test
    @DisplayName("Should assign user to organisations successfully")
    void shouldAssignUserToOrganisationsSuccessfully() {
        String subjectId = "user123";
        List<Long> orgIds = List.of(1L, 2L);

        User user = new User(subjectId, "Ahmed", "Hamed", "Ahmed@example.com", "client123");

        Organisation org1 = new Organisation();
        org1.setId(1L);
        org1.setName("Org1");

        Organisation org2 = new Organisation();
        org2.setId(2L);
        org2.setName("Org2");

        when(userRepository.findWithOrganisations(subjectId)).thenReturn(Optional.of(user));
        when(organisationsRepository.findAllById(orgIds)).thenReturn(List.of(org1, org2));

        when(userRepository.save(any(User.class))).thenAnswer(invocation -> {
            User savedUser = invocation.getArgument(0);
            System.out.println("Save called with relations size: " + savedUser.getUserOrganisationRelations().size());
            return savedUser;
        });

        service.assignUserToOrganisations(subjectId, orgIds);

        verify(userRepository).save(user);

        System.out.println("Final relations size: " + user.getUserOrganisationRelations().size());
        user.getUserOrganisationRelations().forEach(rel ->
                System.out.println("Relation - Org ID: " + rel.getOrganization().getId() + ", Rel ID: " + rel.getId())
        );

        assertEquals(2, user.getUserOrganisationRelations().size());
    }

}