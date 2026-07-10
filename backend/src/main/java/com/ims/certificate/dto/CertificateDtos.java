package com.ims.certificate.dto;

import com.ims.certificate.Certificate;
import com.ims.certificate.CertificateType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;
import java.util.UUID;

public final class CertificateDtos {

    private CertificateDtos() {
    }

    public record IssueCertificate(
            @NotNull UUID studentId,
            @NotNull CertificateType type,
            @NotBlank String title,
            String serialNo,
            LocalDate issueDate,
            String content) {
    }

    public record CertificateResponse(
            UUID id, UUID studentId, CertificateType type, String serialNo,
            String title, LocalDate issueDate, String content) {
        public static CertificateResponse from(Certificate c) {
            return new CertificateResponse(c.getId(), c.getStudentId(), c.getType(),
                    c.getSerialNo(), c.getTitle(), c.getIssueDate(), c.getContent());
        }
    }
}
