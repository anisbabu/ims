package com.ims.report;

import com.ims.report.dto.ReportDtos.AttendanceReport;
import com.ims.report.dto.ReportDtos.ExamResultSheet;
import com.ims.report.dto.ReportDtos.StudentMarksReport;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.UUID;

@RestController
@RequestMapping("/api/reports")
@PreAuthorize("hasAnyRole('SUPER_ADMIN','INSTITUTE_ADMIN','TEACHER')")
public class AcademicReportController {

    private final AcademicReportService reportService;

    public AcademicReportController(AcademicReportService reportService) {
        this.reportService = reportService;
    }

    @GetMapping("/attendance")
    public AttendanceReport attendance(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to,
            @RequestParam(required = false) UUID sectionId) {
        return reportService.attendanceReport(from, to, sectionId);
    }

    @GetMapping("/exam/{examId}/result-sheet")
    public ExamResultSheet examResultSheet(@PathVariable UUID examId) {
        return reportService.examResultSheet(examId);
    }

    @GetMapping("/student/{studentId}/marks")
    public StudentMarksReport studentMarks(@PathVariable UUID studentId) {
        return reportService.studentMarks(studentId);
    }
}
