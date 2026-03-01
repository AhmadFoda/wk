package com.wolterskluwer.backend.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;

@Entity
@Table(name = "credentials", uniqueConstraints = @UniqueConstraint(columnNames = {"client_id", "organisation_id", "status"}))
public class Credentials {
    @Getter
    @Setter
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @Getter
    @Column(name = "client_id", nullable = false)

    private String clientId;

    @Setter
    @Getter
    @Column(name = "client_secret", nullable = false, unique = true)
    private String clientSecret;

    @Column(name = "creation_date", nullable = false)
    private Instant creationDate;

    @Setter
    @Getter
    @Column(name = "expiration_date", nullable = false)
    private Instant expirationDate;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "organisation_id", nullable = false)
    private Organisation organisation;

    @Setter
    @Getter
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private CredentialsStatus status;


    public Credentials() {
    }

    public Credentials(String clientId, String clientSecret, Instant creationDate, Instant expirationDate, CredentialsStatus status) {
        this.clientId = clientId;
        this.clientSecret = clientSecret;
        this.creationDate = creationDate;
        this.expirationDate = expirationDate;
        this.status = status;
    }


    public void setOrganization(Organisation organisation) {
        this.organisation = organisation;
    }

    public enum CredentialsStatus {
        ACTIVE, INACTIVE
    }

}
