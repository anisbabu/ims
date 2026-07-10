package com.ims.exam.dto;

import com.ims.exam.Exam;
import com.ims.exam.ExamStatus;
import com.ims.exam.ExamType;
import com.ims.exam.Mark;
import com.ims.exam.Subject;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public final class ExamDtos {

    private ExamDtos() {
    }

    // ---- Subject ----
    public record CreateSubject(@NotBlank String name, String code) {
    }

    public record SubjectResponse(UUID id, String name, String code) {
        public static SubjectResponse from(Subject s) {
            return new SubjectResponse(s.getId(), s.getName(), s.getCode());
        }
    }

    // ---- Exam type ----
    public record CreateExamType(@NotBlank String name, Integer weightPercent) {
    }

    public record ExamTypeResponse(UUID id, String name, Integer weightPercent) {
        public static ExamTypeResponse from(ExamType t) {
            return new ExamTypeResponse(t.getId(), t.getName(), t.getWeightPercent());
        }
    }

    // ---- Exam ----
    public record CreateExam(
            @NotBlank String name,
            @NotNull UUID examTypeId,
            @NotNull UUID academicYearId,
            UUID gradeId,
            LocalDate startDate,
            LocalDate endDate) {
    }

    public record UpdateExamStatus(@NotNull ExamStatus status) {
    }

    public record ExamResponse(
            UUID id, String name, UUID examTypeId, UUID academicYearId, UUID gradeId,
            LocalDate startDate, LocalDate endDate, ExamStatus status) {
        public static ExamResponse from(Exam e) {
            return new ExamResponse(e.getId(), e.getName(), e.getExamTypeId(), e.getAcademicYearId(),
                    e.getGradeId(), e.getStartDate(), e.getEndDate(), e.getStatus());
        }
    }

    // ---- Marks ----
    public record MarkEntry(
            @NotNull UUID subjectId,
            @NotNull BigDecimal maxMarks,
            @NotNull BigDecimal obtainedMarks,
            String remarks) {
    }

    /** Upsert a batch of a student's subject marks for one exam. */
    public record SaveMarks(
            @NotNull UUID studentId,
            @NotNull List<MarkEntry> entries) {
    }

    public record MarkResponse(
            UUID id, UUID examId, UUID studentId, UUID subjectId,
            BigDecimal maxMarks, BigDecimal obtainedMarks, String remarks) {
        public static MarkResponse from(Mark m) {
            return new MarkResponse(m.getId(), m.getExamId(), m.getStudentId(), m.getSubjectId(),
                    m.getMaxMarks(), m.getObtainedMarks(), m.getRemarks());
        }
    }

    // ---- Marksheet (computed result) ----
    public record MarksheetLine(
            UUID subjectId, String subjectName, BigDecimal maxMarks, BigDecimal obtainedMarks,
            double percent, String letter, boolean pass) {
    }

    public record Marksheet(
            UUID examId, String examName, UUID studentId, String studentName,
            List<MarksheetLine> lines,
            BigDecimal totalMax, BigDecimal totalObtained,
            double percent, double gpa, String letter, boolean pass,
            Integer position) {
    }
}
