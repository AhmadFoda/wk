package com.wolterskluwer.backend.service.implementation;

import com.wolterskluwer.backend.dto.UserDTO;
import com.wolterskluwer.backend.model.User;
import com.wolterskluwer.backend.repository.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtClaimNames;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceImplementationTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private UserServiceImplementation userService;

    @Test
    @DisplayName("Should return existing user when found")
    void shouldReturnExistingUserWhenFound() {
        String subjectId = "user123";
        Jwt jwt = createTestJwt(subjectId);

        User existingUser = new User(subjectId, "John", "Doe", "john@example.com", "client123");
        existingUser.setId(1L);

        when(userRepository.findUserBySubjectId(subjectId)).thenReturn(Optional.of(existingUser));


        UserDTO result = userService.me(jwt);


        assertThat(result.userId()).isEqualTo(1L);
        assertThat(result.firstName()).isEqualTo("John");
        assertThat(result.lastName()).isEqualTo("Doe");
        assertThat(result.email()).isEqualTo("john@example.com");
        assertThat(result.organisations()).isEmpty();
        assertThat(result.requireSelection()).isFalse();

        verify(userRepository).findUserBySubjectId(subjectId);
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    @DisplayName("Should create new user when not found")
    void shouldCreateNewUserWhenNotFound() {
        String subjectId = "newuser123";
        Jwt jwt = createTestJwt(subjectId);

        User newUser = new User(subjectId, "New", "User", "newuser@example.com", UUID.randomUUID().toString());
        newUser.setId(1L);

        when(userRepository.findUserBySubjectId(subjectId)).thenReturn(Optional.empty());
        when(userRepository.save(any(User.class))).thenReturn(newUser);


        UserDTO result = userService.me(jwt);


        assertThat(result.userId()).isEqualTo(1L);
        assertThat(result.firstName()).isEqualTo("New");
        assertThat(result.lastName()).isEqualTo("User");
        assertThat(result.email()).isEqualTo("newuser@example.com");

        verify(userRepository).findUserBySubjectId(subjectId);
        verify(userRepository).save(any(User.class));
    }

    @Test
    @DisplayName("Should extract claims correctly from JWT")
    void shouldExtractClaimsCorrectlyFromJwt() {
        String subjectId = "user123";
        Jwt jwt = createTestJwt(subjectId);

        User newUser = new User(subjectId, "Test", "User", "test@example.com", UUID.randomUUID().toString());
        newUser.setId(1L);

        when(userRepository.findUserBySubjectId(subjectId)).thenReturn(Optional.empty());
        when(userRepository.save(any(User.class))).thenReturn(newUser);

        UserDTO result = userService.me(jwt);

        assertThat(result.firstName()).isEqualTo("Test");
        assertThat(result.lastName()).isEqualTo("User");
        assertThat(result.email()).isEqualTo("test@example.com");

        verify(userRepository).save(argThat(user ->
                user.getSubjectId().equals(subjectId) &&
                        user.getFirstName().equals("Test") &&
                        user.getLastName().equals("User") &&
                        user.getEmail().equals("test@example.com")
        ));
    }

    private Jwt createTestJwt(String subjectId) {
        return Jwt.withTokenValue("test-token")
                .header("alg", "none")
                .claim(JwtClaimNames.SUB, subjectId)
                .claim("name", "Test")
                .claim("family_name", "User")
                .claim("email", "test@example.com")
                .build();
    }
}