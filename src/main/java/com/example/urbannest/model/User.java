package com.example.urbannest.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import java.util.UUID;

import java.time.OffsetDateTime;

@Entity
@Table(name = "users")
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "user_id", updatable = false, nullable = false)
    @Getter @Setter
    private UUID userId;

    @Column(name = "firebase_uid", unique = true, nullable = false)
    @Setter
    private String firebaseId;

    @Column(name = "name", nullable = false)
    @Getter @Setter
    private String name;

    @Column(name = "email", unique = true, nullable = false)
    @Getter @Setter
    private String email;

    @Column(name = "phone", unique = true)
    @Getter @Setter
    private String phone;

    @Column(name = "nid_hash", unique = true, nullable = false)
    @Setter
    private String nidHash;

    @Column(name = "nid_encrypted")
    @Getter @Setter
    private String nidEncrypted;

    @Column(name = "created_at", nullable = false)
    @Getter @Setter
    private OffsetDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    @Getter @Setter
    private OffsetDateTime updatedAt;

    @Column(name = "deleted_at")
    @Getter @Setter
    private OffsetDateTime deletedAt;

    @Column(name = "role_name", nullable = false)
    @Getter @Setter
    private String roleName;

    @Column(name = "profile_picture_url")
    @Getter @Setter
    private String profilePictureUrl;
}
