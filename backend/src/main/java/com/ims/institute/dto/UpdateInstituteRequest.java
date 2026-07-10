package com.ims.institute.dto;

import jakarta.validation.constraints.Email;

public record UpdateInstituteRequest(
        String name,
        String address,
        String phone,
        @Email String email,
        String logoUrl,
        String settings
) {
}
