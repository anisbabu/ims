package com.ims.auth.dto;

import com.ims.auth.Role;

import java.util.UUID;

public record MeResponse(
        UUID id,
        String email,
        String fullName,
        Role role,
        UUID instituteId,
        UUID profileId
) {
}
