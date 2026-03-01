package com.wolterskluwer.backend.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.HashSet;
import java.util.List;
import java.util.Set;


@Entity
@Table(name = "organisation",
        uniqueConstraints = {
                @UniqueConstraint(columnNames = "vat_number"),
                @UniqueConstraint(columnNames = "sap_id")
        })
public class Organisation {
    @Getter
    @Setter
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @Getter
    @Setter
    @Column(nullable = false)
    private String name;

    @Getter
    @Column(name = "vat_number", nullable = false, unique = true)
    private String vatNumber;

    @Getter
    @Column(name = "sap_id", nullable = false, unique = true)
    private String sapId;

    @OneToMany(mappedBy = "organisation", cascade = CascadeType.ALL)
    private List<Credentials> credentials;


    @Getter
    @OneToMany(mappedBy = "organisation", orphanRemoval = true)
    private Set<UserOrganisationRelation> relations = new HashSet<>();


}


