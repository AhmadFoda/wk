package com.wolterskluwer.backend.repository;

import com.wolterskluwer.backend.model.User;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    @EntityGraph(attributePaths = {"userOrganisationRelations"})
    Optional<User> findUserBySubjectId(String subjectId);

    @Query("SELECT u FROM User u LEFT JOIN FETCH u.userOrganisationRelations r LEFT JOIN FETCH r.organisation WHERE u.subjectId = :subjectId")
    Optional<User> findWithOrganisations(@Param("subjectId") String subjectId);
}
