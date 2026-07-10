package com.ims.auth.dto;

import com.ims.auth.Role;
import com.ims.auth.User;
import com.ims.auth.UserStatus;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.Instant;
import java.util.UUID;

public final class UserDtos {

    private UserDtos() {
    }

    public record CreateUser(
            @NotBlank @Email String email,
            @NotBlank @Size(min = 8) String password,
            @NotBlank String fullName,
            @NotNull Role role,
            /** Only honoured for SUPER_ADMIN; institute admins always create within their own tenant. */
            UUID instituteId,
            UUID profileId) {
    }

    public record UpdateUser(
            String fullName,
            Role role,
            UserStatus status,
            UUID profileId) {
    }

    public record ResetPassword(
            @NotBlank @Size(min = 8) String newPassword) {
    }

    public record ChangePassword(
            @NotBlank String currentPassword,
            @NotBlank @Size(min = 8) String newPassword) {
    }

    public record UserResponse(
            UUID id, String email, String fullName, Role role, UserStatus status,
            UUID instituteId, UUID profileId, Instant createdAt) {
        public static UserResponse from(User u) {
            return new UserResponse(u.getId(), u.getEmail(), u.getFullName(), u.getRole(),
                    u.getStatus(), u.getInstituteId(), u.getProfileId(), u.getCreatedAt());
        }
    }
}
