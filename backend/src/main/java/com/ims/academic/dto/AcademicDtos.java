package com.ims.academic.dto;

import com.ims.academic.AcademicYear;
import com.ims.academic.Grade;
import com.ims.academic.Section;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;
import java.util.UUID;

public final class AcademicDtos {

    private AcademicDtos() {
    }

    // ---- Academic Year ----
    public record CreateYear(
            @NotBlank String name,
            LocalDate startDate,
            LocalDate endDate,
            boolean current) {
    }

    public record UpdateYear(
            String name,
            LocalDate startDate,
            LocalDate endDate,
            Boolean current) {
    }

    public record YearResponse(
            UUID id, String name, LocalDate startDate, LocalDate endDate, boolean current) {
        public static YearResponse from(AcademicYear y) {
            return new YearResponse(y.getId(), y.getName(), y.getStartDate(), y.getEndDate(), y.isCurrent());
        }
    }

    // ---- Grade ----
    public record CreateGrade(@NotBlank String name, int orderNo) {
    }

    public record UpdateGrade(String name, Integer orderNo) {
    }

    public record GradeResponse(UUID id, String name, int orderNo) {
        public static GradeResponse from(Grade g) {
            return new GradeResponse(g.getId(), g.getName(), g.getOrderNo());
        }
    }

    // ---- Section ----
    public record CreateSection(
            @NotNull UUID gradeId,
            @NotBlank String name,
            UUID classTeacherId,
            int capacity) {
    }

    public record UpdateSection(
            String name,
            UUID classTeacherId,
            Integer capacity) {
    }

    public record SectionResponse(
            UUID id, UUID gradeId, String name, UUID classTeacherId, int capacity) {
        public static SectionResponse from(Section s) {
            return new SectionResponse(s.getId(), s.getGradeId(), s.getName(),
                    s.getClassTeacherId(), s.getCapacity());
        }
    }
}
