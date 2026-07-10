package com.ims.institute.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CreateInstituteRequest(
        @NotBlank String name,
        @NotBlank @Size(max = 64) String code,
        String address,
        String phone,
        @Email String email,
        @NotBlank @Email String adminEmail,
        @NotBlank @Size(min = 8) String adminPassword,
        @NotBlank String adminFullName
) {
}
