package com.wolterskluwer.backend.service.implementation;

import com.wolterskluwer.backend.exception.ForbiddenOperationException;
import com.wolterskluwer.backend.model.Organisation;
import com.wolterskluwer.backend.model.User;
import com.wolterskluwer.backend.model.UserOrganisationRelation;
import com.wolterskluwer.backend.repository.UserRepository;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Optional;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CredentialsValidationServiceImplementationTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private CredentialsValidationServiceImplementation validationService;

    @Test
    @DisplayName("Should validate user access successfully")
    void shouldValidateUserAccessSuccessfully() {
        String subjectId = "user123";
        long orgId = 1L;

        User user = new User(subjectId, "ahmed", "hamed", "ahmed@example.com", "client123");
        Organisation organisation = new Organisation();
        organisation.setId(orgId);
        UserOrganisationRelation relation = new UserOrganisationRelation(user, organisation);

        user.getUserOrganisationRelations().add(relation);

        when(userRepository.findWithOrganisations(subjectId)).thenReturn(Optional.of(user));

        User result = validationService.validateUserAccess(subjectId, orgId);

        assertThat(result).isNotNull();
        assertThat(result.getSubjectId()).isEqualTo(subjectId);
    }

    @Test
    @DisplayName("Should throw exception when user not found")
    void shouldThrowExceptionWhenUserNotFound() {

        String subjectId = "none";
        long orgId = 1L;

        when(userRepository.findWithOrganisations(subjectId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> validationService.validateUserAccess(subjectId, orgId))
                .isInstanceOf(ForbiddenOperationException.class)
                .hasMessage("User not found");
    }

    @Test
    @DisplayName("Should validate expiration date successfully")
    void shouldValidateExpirationDateSuccessfully() {
        Instant validDate = Instant.now().plus(30, ChronoUnit.DAYS);

        boolean result = validationService.isExpirationDateValid(validDate);

        assertThat(result).isTrue();
    }

    @Test
    @DisplayName("Should throw exception for past expiration date")
    void shouldThrowExceptionForPastExpirationDate() {
        Instant pastDate = Instant.now().minus(1, ChronoUnit.DAYS);


        assertThatThrownBy(() -> validationService.isExpirationDateValid(pastDate))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Expiration date cannot be in the past.");
    }

    @Test
    @DisplayName("Should throw exception for expiration date too far in future")
    void shouldThrowExceptionForTooFarFutureDate() {
        Instant tooFarFuture = Instant.now().plus(100, ChronoUnit.DAYS);

        assertThatThrownBy(() -> validationService.isExpirationDateValid(tooFarFuture))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Expiration date cannot be more than 90 days.");
    }
}