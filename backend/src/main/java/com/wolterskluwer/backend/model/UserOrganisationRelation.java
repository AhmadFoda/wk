package com.wolterskluwer.backend.model;

import jakarta.persistence.*;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "user_org_memberships",
        uniqueConstraints = @UniqueConstraint(columnNames = {"user_id", "organisation_id"}))
public class UserOrganisationRelation {


    @Getter
    @Setter
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @Getter
    @Setter
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;


    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "organisation_id", nullable = false)
    private Organisation organisation;

    public UserOrganisationRelation(User user, Organisation organisation) {
        this.user = user;
        this.organisation = organisation;
    }

    public UserOrganisationRelation() {

    }

    public Organisation getOrganization() {
        return organisation;
    }

}
