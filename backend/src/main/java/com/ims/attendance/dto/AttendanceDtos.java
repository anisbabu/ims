package com.ims.attendance.dto;

import com.ims.attendance.Attendance;
import com.ims.attendance.AttendanceStatus;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public final class AttendanceDtos {

    private AttendanceDtos() {
    }

    public record MarkEntry(
            @NotNull UUID studentId,
            @NotNull AttendanceStatus status,
            String remarks) {
    }

    /** Bulk-mark attendance for a date (optionally a section). Upserts per student. */
    public record MarkAttendance(
            @NotNull LocalDate date,
            UUID sectionId,
            @NotNull List<MarkEntry> entries) {
    }

    public record AttendanceResponse(
            UUID id, UUID studentId, UUID sectionId, LocalDate date,
            AttendanceStatus status, String remarks) {
        public static AttendanceResponse from(Attendance a) {
            return new AttendanceResponse(a.getId(), a.getStudentId(), a.getSectionId(),
                    a.getDate(), a.getStatus(), a.getRemarks());
        }
    }

    public record AttendanceSummary(
            UUID studentId, LocalDate from, LocalDate to,
            int totalDays, Map<AttendanceStatus, Long> counts, double presentPercent) {
    }
}
