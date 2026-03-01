package com.wolterskluwer.backend.service.implementation;

import com.wolterskluwer.backend.model.Credentials;
import com.wolterskluwer.backend.repository.CredentialsRepository;
import com.wolterskluwer.backend.service.CredentialsLifeCycleService;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;

@Service
@RequiredArgsConstructor
public class CredentialsLifeCycleServiceImplementation implements CredentialsLifeCycleService {

    private final CredentialsRepository credentialsRepository;

    @Override
    @Scheduled(cron = "0 0 1 * * *")
    public void checkExpiredCredentials() {
        List<Credentials> expired = credentialsRepository.findByStatusAndExpirationDateBefore(
                Credentials.CredentialsStatus.ACTIVE, Instant.now());
        expired.forEach(credential -> {
            credential.setStatus(Credentials.CredentialsStatus.INACTIVE);
            credentialsRepository.save(credential);
        });
    }
}
