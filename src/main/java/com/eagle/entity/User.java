package com.eagle.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.time.Instant;

@Table(name = "Eagle_User")
@Entity
@Data
public class User {

    public User(String userName, Address address, String phoneNumber, String email, String passwordHash) {
        this.userName = userName;
        this.address = address;
        this.phoneNumber = phoneNumber;
        this.email = email;
        this.passwordHash = passwordHash;
    }

    @Id
    @NotBlank
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "user_id", nullable = false, unique = true)
    private String userId;

    @Column(name = "user_name", nullable = false)
    private String userName;

    @Embedded
    private Address address;

    @Column(name = "phone_number", nullable = false)
    private String phoneNumber;

    @Column(name = "email", nullable = false, unique = true)
    private String email;

    @Column(name = "password_hash", nullable = false)
    private String passwordHash;

    @Column(name = "created_timestamp", nullable = false, updatable = false)
    private Instant createdTimestamp;

    @Column(name = "updated_timestamp", nullable = false)
    private Instant updatedTimestamp;

    public User() {
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedTimestamp = Instant.now();
    }

    @PrePersist
    protected void onCreate() {
        this.createdTimestamp = Instant.now();
        this.updatedTimestamp = Instant.now();
    }

    @Data
    @Embeddable
    public static class Address {
        @Column(name = "address_line1", nullable = false)
        private String line1;

        @Column(name = "address_line2")
        private String line2;

        @Column(name = "address_line3")
        private String line3;

        @Column(name = "town", nullable = false)
        private String town;

        @Column(name = "county", nullable = false)
        private String county;

        @Column(name = "postcode", nullable = false)
        private String postcode;

        public Address(String line1, String line2, String line3,
                       String town, String county, String postcode) {
            this.line1 = line1;
            this.line2 = line2;
            this.line3 = line3;
            this.town = town;
            this.county = county;
            this.postcode = postcode;
        }

        public Address() {
        }
    }
}
