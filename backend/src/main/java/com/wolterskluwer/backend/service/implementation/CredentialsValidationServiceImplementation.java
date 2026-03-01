package com.wolterskluwer.backend.service.implementation;

import com.wolterskluwer.backend.exception.ForbiddenOperationException;
import com.wolterskluwer.backend.model.User;
import com.wolterskluwer.backend.repository.UserRepository;
import com.wolterskluwer.backend.service.CredentialsValidationService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

@Service
@RequiredArgsConstructor
public class CredentialsValidationServiceImplementation implements CredentialsValidationService {
    private final UserRepository userRepository;

    @Override
    public User validateUserAccess(String subjectId, long organisationId) {
        User user = userRepository.findWithOrganisations(subjectId)
                .orElseThrow(() -> new ForbiddenOperationException("User not found"));

        boolean isMember = user.getUserOrganisationRelations().stream()
                .anyMatch(relation -> relation.getOrganization().getId() == organisationId);
        if (!isMember) throw new ForbiddenOperationException("User is not member of organisation");
        return user;
    }

    @Override
    public boolean isExpirationDateValid(Instant expiresAt) {
        final Instant max = Instant.now().plus(90, ChronoUnit.DAYS);
        if (expiresAt == null) return false;
        if (expiresAt.isAfter(max))
            throw new IllegalArgumentException("Expiration date cannot be more than 90 days.");
        if (expiresAt.isBefore(Instant.now()))
            throw new IllegalArgumentException("Expiration date cannot be in the past.");
        return true;
    }
}
