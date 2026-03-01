package com.wolterskluwer.backend.service.implementation;

import com.wolterskluwer.backend.service.SecretGenerationService;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.util.Base64;

@Service
public class SecretGenerationServiceImplementation implements SecretGenerationService {
    @Override
    public String generateSecret() {
        byte[] secretBytes = new byte[32];
        new SecureRandom().nextBytes(secretBytes);
        return Base64.getUrlEncoder().encodeToString(secretBytes);
    }
}
