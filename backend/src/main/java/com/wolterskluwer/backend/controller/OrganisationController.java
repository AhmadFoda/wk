package com.wolterskluwer.backend.controller;

import com.wolterskluwer.backend.dto.AssignOrganisationRequestDTO;
import com.wolterskluwer.backend.dto.OrganisationDTO;
import com.wolterskluwer.backend.service.OrganisationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/organisations")
@RequiredArgsConstructor
public class OrganisationController {
    private final OrganisationService organisationService;

    @GetMapping
    public List<OrganisationDTO> getAllOrganizations() {
        return organisationService.getAllOrganizations();
    }

    @PostMapping("/assign")
    public ResponseEntity<Void> assignOrganisation(@AuthenticationPrincipal Jwt jwt, @RequestBody AssignOrganisationRequestDTO request) {
        organisationService.assignUserToOrganisations(jwt.getSubject(), request.organisationIds());
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{organisationId}")
    public ResponseEntity<Void> removeOrganisation(@AuthenticationPrincipal Jwt jwt, @PathVariable Long organisationId) {
        organisationService.removeUserFromOrganisation(jwt.getSubject(), organisationId);
        return ResponseEntity.ok().build();
    }
}
