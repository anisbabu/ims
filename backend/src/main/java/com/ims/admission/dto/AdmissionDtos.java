package com.ims.admission.dto;

import com.ims.admission.Admission;
import com.ims.admission.AdmissionStatus;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;
import java.util.UUID;

public final class AdmissionDtos {

    private AdmissionDtos() {
    }

    public record CreateAdmission(
            @NotNull UUID studentId,
            @NotNull UUID academicYearId,
            @NotNull UUID gradeId,
            UUID sectionId,
            String admissionNo,
            LocalDate admissionDate) {
    }

    public record UpdateStatus(
            @NotNull AdmissionStatus status) {
    }

    public record AdmissionResponse(
            UUID id, UUID studentId, UUID academicYearId, UUID gradeId, UUID sectionId,
            String admissionNo, LocalDate admissionDate, AdmissionStatus status) {
        public static AdmissionResponse from(Admission a) {
            return new AdmissionResponse(a.getId(), a.getStudentId(), a.getAcademicYearId(),
                    a.getGradeId(), a.getSectionId(), a.getAdmissionNo(), a.getAdmissionDate(),
                    a.getStatus());
        }
    }
}
