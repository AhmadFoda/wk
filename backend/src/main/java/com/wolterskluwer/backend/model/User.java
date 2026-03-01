package com.wolterskluwer.backend.model;

import jakarta.persistence.*;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.Set;


@Entity
@Table(name = "users", uniqueConstraints = @UniqueConstraint(columnNames = "subject_id"))
public class User {
    @Setter
    @Getter
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @Setter
    @Getter
    @Column(name = "subject_id", nullable = false, length = 128, updatable = false)
    private String subjectId;

    @Getter
    @Column(nullable = false, name = "first_name")
    private String firstName;

    @Getter
    @Column(nullable = false, name = "last_name")
    private String lastName;

    @Getter
    @Column(nullable = false, unique = true)
    private String email;

    @Getter
    @Column(name = "client_id", nullable = false, unique = true)
    private String clientId;

    @Getter
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL)
    private Set<UserOrganisationRelation> userOrganisationRelations = new HashSet<>();


    public User() {
    }

    public User(String subjectId, String firstName, String lastName, String email, String clientId) {
        this.subjectId = subjectId;
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
        this.clientId = clientId;
    }

    @Transactional
    public void addOrganisation(Organisation organisation) {
        boolean exists = this.getUserOrganisationRelations().stream().anyMatch(relation -> relation.getOrganization().getId() == organisation.getId());
        if (exists) return;
        UserOrganisationRelation relation = new UserOrganisationRelation(this, organisation);
        this.getUserOrganisationRelations().add(relation);
        organisation.getRelations().add(relation);
    }


}
