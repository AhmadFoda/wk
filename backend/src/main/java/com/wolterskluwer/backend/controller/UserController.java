package com.wolterskluwer.backend.controller;

import com.wolterskluwer.backend.dto.OrganisationDTO;
import com.wolterskluwer.backend.dto.UserDTO;
import com.wolterskluwer.backend.service.OrganisationService;
import com.wolterskluwer.backend.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.oauth2.jwt.Jwt;

import java.util.List;

@RestController
@RequestMapping("/api/v1/user")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    private final OrganisationService organisationService;

    @GetMapping("/me")
    public ResponseEntity<UserDTO> me(@AuthenticationPrincipal Jwt jwt) {
        return ResponseEntity.ok(userService.me(jwt));
    }


    @GetMapping("/organisations")
    public ResponseEntity<List<OrganisationDTO>> getUserOrganisation(@AuthenticationPrincipal Jwt jwt) {
        return ResponseEntity.ok().body(organisationService.getUserOrganisation(jwt.getSubject()));
    }


}
