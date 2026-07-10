package com.ims.auth;

import com.ims.common.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

/**
 * Authentication principal. Not tenant-filtered (login resolves users by globally
 * unique email before a tenant context exists). instituteId is null for SUPER_ADMIN.
 * profileId optionally links to a Student/Teacher/Guardian row.
 */
@Getter
@Setter
@Entity
@Table(name = "app_user")
public class User extends BaseEntity {

    @Column(nullable = false, unique = true)
    private String email;

    @Column(name = "password_hash", nullable = false)
    private String passwordHash;

    @Column(name = "full_name", nullable = false)
    private String fullName;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 32)
    private Role role;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 16)
    private UserStatus status = UserStatus.ACTIVE;

    @Column(name = "institute_id")
    private UUID instituteId;

    @Column(name = "profile_id")
    private UUID profileId;
}
