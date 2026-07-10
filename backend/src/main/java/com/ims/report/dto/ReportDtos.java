package com.ims.report.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public final class ReportDtos {

    private ReportDtos() {
    }

    // ---- Attendance report ----
    public record AttendanceRow(
            UUID studentId, String studentName,
            long present, long absent, long late, long excused, long holiday,
            long totalDays, double presentPercent) {
    }

    public record AttendanceReport(
            LocalDate from, LocalDate to, UUID sectionId, List<AttendanceRow> rows) {
    }

    // ---- Exam result sheet (whole class) ----
    public record ExamResultRow(
            UUID studentId, String studentName, int subjects,
            BigDecimal totalMax, BigDecimal totalObtained,
            double percent, double gpa, String letter, boolean pass, int position) {
    }

    public record ExamResultSheet(
            UUID examId, String examName, List<ExamResultRow> rows) {
    }

    // ---- Student marks history ----
    public record StudentMarkRow(
            UUID examId, String examName,
            BigDecimal totalMax, BigDecimal totalObtained,
            double percent, String letter, boolean pass) {
    }

    public record StudentMarksReport(
            UUID studentId, String studentName, List<StudentMarkRow> rows) {
    }
}
