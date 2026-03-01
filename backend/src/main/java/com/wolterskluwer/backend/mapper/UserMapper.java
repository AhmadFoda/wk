package com.wolterskluwer.backend.mapper;

import com.wolterskluwer.backend.dto.OrganisationDTO;
import com.wolterskluwer.backend.dto.UserDTO;
import com.wolterskluwer.backend.model.User;
import com.wolterskluwer.backend.model.UserOrganisationRelation;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class UserMapper {
    private UserMapper() {
    }

    public static UserDTO toDto(User user) {
        Set<UserOrganisationRelation> userOrganisationRelationSet = user.getUserOrganisationRelations();
        List<OrganisationDTO> organisationList = new ArrayList<>();
        userOrganisationRelationSet.forEach(userOrgRel -> organisationList.add(OrganisationMapper.toDto(userOrgRel.getOrganization())));
        return new UserDTO(user.getId(), user.getFirstName(), user.getLastName(), user.getEmail(), organisationList, !organisationList.isEmpty());
    }
}
