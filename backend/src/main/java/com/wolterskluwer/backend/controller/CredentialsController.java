package com.wolterskluwer.backend.controller;

import com.wolterskluwer.backend.dto.CredentialsRequest;
import com.wolterskluwer.backend.dto.CredentialsResponse;
import com.wolterskluwer.backend.service.CredentialsService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/credentials")
@RequiredArgsConstructor
public class CredentialsController {


    private final CredentialsService credentialsService;

    @GetMapping("/organisations/{orgId}")
    public ResponseEntity<CredentialsResponse> getCredentials(@AuthenticationPrincipal Jwt jwt, @PathVariable Long orgId) {
        return ResponseEntity.ok(credentialsService.getCredentials(jwt.getSubject(), orgId));
    }

    @PostMapping("/organisations/{orgId}")
    public ResponseEntity<CredentialsResponse> createCredentials(@AuthenticationPrincipal Jwt jwt,
                                                                 @PathVariable Long orgId,
                                                                 @RequestBody(required = false)
                                                                 CredentialsRequest createCredentialsRequest) {

        return ResponseEntity.ok(credentialsService.createCredentials(jwt.getSubject(), orgId, createCredentialsRequest != null ? createCredentialsRequest.expiresAt() : null));
    }

    @PutMapping("/organisations/{orgId}")
    public ResponseEntity<CredentialsResponse> updateCredentials(@AuthenticationPrincipal Jwt jwt,
                                                                 @PathVariable Long orgId,
                                                                 @RequestBody(required = false) CredentialsRequest credentialsRequest) {

        return ResponseEntity.ok(credentialsService.updateCredentials(jwt.getSubject(), orgId, credentialsRequest != null ? credentialsRequest.expiresAt() : null));
    }

    @DeleteMapping("/organisations/{orgId}")
    public ResponseEntity<Void> deleteCredentials(@AuthenticationPrincipal Jwt jwt, @PathVariable Long orgId) {
        credentialsService.deleteCredentials(jwt.getSubject(), orgId);
        return ResponseEntity.status(204).build();
    }
}
