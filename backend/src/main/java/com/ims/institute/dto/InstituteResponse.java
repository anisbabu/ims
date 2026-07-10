package com.ims.institute.dto;

import com.ims.institute.Institute;
import com.ims.institute.InstituteStatus;

import java.util.UUID;

public record InstituteResponse(
        UUID id,
        String name,
        String code,
        String address,
        String phone,
        String email,
        String logoUrl,
        String settings,
        InstituteStatus status
) {
    public static InstituteResponse from(Institute i) {
        return new InstituteResponse(i.getId(), i.getName(), i.getCode(), i.getAddress(),
                i.getPhone(), i.getEmail(), i.getLogoUrl(), i.getSettings(), i.getStatus());
    }
}
