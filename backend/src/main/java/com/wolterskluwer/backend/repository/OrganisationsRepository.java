package com.wolterskluwer.backend.repository;

import com.wolterskluwer.backend.model.Organisation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface OrganisationsRepository extends JpaRepository<Organisation, Long> {
    Optional<Organisation> findByName(String name);

    Optional<Organisation> findBySapId(String sapId);

    Optional<Organisation> findByVatNumber(String vatNumber);

    //    @Query("""
//        select o
//        from Organisation o
//        join o.relations r
//        join r.user u
//        where u.subjectId = :subjectId
//    """)
//    List<Organisation> findAllByUserSubjectId(@Param("subjectId") String subjectId);
    @Query("""
            SELECT DISTINCT o
            FROM Organisation o
            LEFT JOIN FETCH o.relations r
            LEFT JOIN FETCH r.user u
            LEFT JOIN FETCH o.credentials c
            WHERE u.subjectId = :subjectId
            """)
    List<Organisation> findAllByUserSubjectIdWithDetails(@Param("subjectId") String subjectId);
}
