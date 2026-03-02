package com.wolterskluwer.backend.service.implementation;


import com.wolterskluwer.backend.dto.CredentialsResponse;
import com.wolterskluwer.backend.exception.ForbiddenOperationException;
import com.wolterskluwer.backend.model.Credentials;
import com.wolterskluwer.backend.model.User;
import com.wolterskluwer.backend.repository.CredentialsRepository;
import com.wolterskluwer.backend.repository.OrganisationsRepository;
import com.wolterskluwer.backend.service.CredentialsService;
import com.wolterskluwer.backend.service.CredentialsValidationService;
import com.wolterskluwer.backend.service.SecretGenerationService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.wolterskluwer.backend.security.EncryptionService;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

@Service
@RequiredArgsConstructor
public class CredentialsServiceImplementation implements CredentialsService {

    private final OrganisationsRepository organisationsRepository;

    private final CredentialsRepository credentialsRepository;

    private final SecretGenerationService secretGenerationService;

    private final CredentialsValidationService credentialsValidationService;

    private final EncryptionService encryptionService;

    @Override
    public CredentialsResponse createCredentials(String subjectId, long organisationId, Instant expiresAt) {

        return getNewCredentials(organisationId, getExpirationDate(expiresAt), credentialsValidationService.validateUserAccess(subjectId, organisationId));
    }

    @Override
    public CredentialsResponse getCredentials(String subjectId, long organisationId) {
        final Credentials credentials = getCredentialsBySubjectIdAndOrgId(subjectId, organisationId);
        if (credentials == null) throw new ForbiddenOperationException("Credentials not found");
        return new CredentialsResponse(credentials.getId(), credentials.getClientId(), "***", credentials.getExpirationDate(), credentials.getStatus());
    }

    @Override
    @Transactional
    public CredentialsResponse updateCredentials(String subjectId, long organisationId, Instant expiresAt) {
        final Credentials credentials = getCredentialsBySubjectIdAndOrgId(subjectId, organisationId);
        if (credentials == null) throw new ForbiddenOperationException("Credentials not found");
        final String clientSecret = secretGenerationService.generateSecret();
        final Instant expirationDate = getExpirationDate(expiresAt);
        credentials.setClientSecret(encryptionService.encrypt(clientSecret));
        credentials.setExpirationDate(expirationDate);
        credentialsRepository.save(credentials);
        return new CredentialsResponse(credentials.getId(), credentials.getClientId(), clientSecret, expirationDate, credentials.getStatus());
    }

    private CredentialsResponse getNewCredentials(long organisationId, Instant expiresAt, User user) {
        if (credentialsRepository.existsActiveByClientIdAndOrganisationId(user.getClientId(), organisationId))
            throw new ForbiddenOperationException("Credentials already exist.");
        final String clientSecret = secretGenerationService.generateSecret();
        Credentials credentials = new Credentials(user.getClientId(), encryptionService.encrypt(clientSecret), Instant.now(), expiresAt, Credentials.CredentialsStatus.ACTIVE);
        credentials.setOrganization(organisationsRepository.findById(organisationId).orElseThrow(() -> new ForbiddenOperationException("Organisation not found")));
        credentialsRepository.save(credentials);
        return new CredentialsResponse(credentials.getId(), credentials.getClientId(), clientSecret, getExpirationDate(expiresAt), credentials.getStatus());
    }

    @Override
    @Transactional
    public void deleteCredentials(String subjectId, long organisationId) {
        credentialsRepository.deleteByClientIdAndOrganisationId(credentialsValidationService.validateUserAccess(subjectId, organisationId).getClientId(), organisationId);
    }

    private Credentials getCredentialsBySubjectIdAndOrgId(String subjectId, long organisationId) {
        return credentialsRepository.findByClientIdAndOrganisationIdAndStatus(credentialsValidationService.validateUserAccess(subjectId, organisationId).getClientId(), organisationId, Credentials.CredentialsStatus.ACTIVE);
    }

    private Instant getExpirationDate(Instant expiresAt) {
        return credentialsValidationService.isExpirationDateValid(expiresAt) ? expiresAt : Instant.now().plus(90, ChronoUnit.DAYS);
    }

}
