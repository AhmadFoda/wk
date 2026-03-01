package com.wolterskluwer.backend.repository;

import com.wolterskluwer.backend.model.Credentials;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.Instant;
import java.util.List;

public interface CredentialsRepository extends JpaRepository<Credentials, Long> {
    boolean existsActiveByClientIdAndOrganisationId(String clientId, Long organisationId);

    Credentials findByClientIdAndOrganisationIdAndStatus(String clientId, Long organisationId, Credentials.CredentialsStatus status);

    void deleteByClientIdAndOrganisationId(String userId, Long organisationId);

    List<Credentials> findByStatusAndExpirationDateBefore(Credentials.CredentialsStatus status, Instant expirationDate);
}
