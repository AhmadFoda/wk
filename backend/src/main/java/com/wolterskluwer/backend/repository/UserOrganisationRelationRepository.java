package com.wolterskluwer.backend.repository;

import com.wolterskluwer.backend.model.UserOrganisationRelation;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserOrganisationRelationRepository extends JpaRepository<UserOrganisationRelation, Long> {
    boolean existsByUser_SubjectIdAndOrganisation_Id(String subjectId, Long organisationId);
    long deleteByUser_SubjectIdAndOrganisation_Id(String subjectId, Long organisationId);
}
