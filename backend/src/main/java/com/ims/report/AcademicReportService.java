package com.ims.report;

import com.ims.attendance.Attendance;
import com.ims.attendance.AttendanceRepository;
import com.ims.attendance.AttendanceStatus;
import com.ims.common.BadRequestException;
import com.ims.common.NotFoundException;
import com.ims.exam.Exam;
import com.ims.exam.ExamRepository;
import com.ims.exam.GradeScale;
import com.ims.exam.Mark;
import com.ims.exam.MarkRepository;
import com.ims.people.Student;
import com.ims.people.StudentRepository;
import com.ims.report.dto.ReportDtos.AttendanceReport;
import com.ims.report.dto.ReportDtos.AttendanceRow;
import com.ims.report.dto.ReportDtos.ExamResultRow;
import com.ims.report.dto.ReportDtos.ExamResultSheet;
import com.ims.report.dto.ReportDtos.StudentMarkRow;
import com.ims.report.dto.ReportDtos.StudentMarksReport;
import com.ims.tenant.TenantGuard;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class AcademicReportService {

    private final AttendanceRepository attendanceRepository;
    private final MarkRepository markRepository;
    private final ExamRepository examRepository;
    private final StudentRepository studentRepository;

    public AcademicReportService(AttendanceRepository attendanceRepository,
                                 MarkRepository markRepository,
                                 ExamRepository examRepository,
                                 StudentRepository studentRepository) {
        this.attendanceRepository = attendanceRepository;
        this.markRepository = markRepository;
        this.examRepository = examRepository;
        this.studentRepository = studentRepository;
    }

    private Map<UUID, String> studentNames() {
        return studentRepository.findAll().stream()
                .collect(Collectors.toMap(Student::getId, Student::getFullName));
    }

    // ---- Attendance report ----

    @Transactional(readOnly = true)
    public AttendanceReport attendanceReport(LocalDate from, LocalDate to, UUID sectionId) {
        if (from == null || to == null || to.isBefore(from)) {
            throw new BadRequestException("Invalid date range");
        }
        List<Attendance> records = sectionId != null
                ? attendanceRepository.findByDateBetweenAndSectionId(from, to, sectionId)
                : attendanceRepository.findByDateBetween(from, to);

        Map<UUID, String> names = studentNames();
        Map<UUID, List<Attendance>> byStudent = records.stream()
                .collect(Collectors.groupingBy(Attendance::getStudentId));

        List<AttendanceRow> rows = new ArrayList<>();
        for (var e : byStudent.entrySet()) {
            long present = 0, absent = 0, late = 0, excused = 0, holiday = 0;
            for (Attendance a : e.getValue()) {
                switch (a.getStatus()) {
                    case PRESENT -> present++;
                    case ABSENT -> absent++;
                    case LATE -> late++;
                    case EXCUSED -> excused++;
                    case HOLIDAY -> holiday++;
                }
            }
            long total = e.getValue().size();
            long attended = present + late;
            double pct = total == 0 ? 0 : round((attended * 100.0) / total);
            rows.add(new AttendanceRow(e.getKey(), names.getOrDefault(e.getKey(), "?"),
                    present, absent, late, excused, holiday, total, pct));
        }
        rows.sort(Comparator.comparing(AttendanceRow::studentName));
        return new AttendanceReport(from, to, sectionId, rows);
    }

    // ---- Exam result sheet (whole class, ranked) ----

    @Transactional(readOnly = true)
    public ExamResultSheet examResultSheet(UUID examId) {
        Exam exam = examRepository.findById(examId).map(TenantGuard::owned)
                .orElseThrow(() -> new NotFoundException("Exam not found"));
        Map<UUID, String> names = studentNames();
        Map<UUID, List<Mark>> byStudent = markRepository.findByExamId(examId).stream()
                .collect(Collectors.groupingBy(Mark::getStudentId));

        // First compute totals per student, then rank by percent.
        record Agg(UUID studentId, int subjects, BigDecimal max, BigDecimal obt, double gpSum) {
        }
        List<Agg> aggs = new ArrayList<>();
        for (var e : byStudent.entrySet()) {
            BigDecimal max = BigDecimal.ZERO, obt = BigDecimal.ZERO;
            double gpSum = 0;
            for (Mark m : e.getValue()) {
                max = max.add(m.getMaxMarks());
                obt = obt.add(m.getObtainedMarks());
                gpSum += GradeScale.gradePoint(percent(m.getObtainedMarks(), m.getMaxMarks()));
            }
            aggs.add(new Agg(e.getKey(), e.getValue().size(), max, obt, gpSum));
        }
        aggs.sort(Comparator.comparingDouble((Agg a) ->
                a.max().signum() == 0 ? 0 : percent(a.obt(), a.max())).reversed());

        List<ExamResultRow> rows = new ArrayList<>();
        int pos = 0;
        for (Agg a : aggs) {
            pos++;
            double pct = a.max().signum() == 0 ? 0 : percent(a.obt(), a.max());
            double gpa = a.subjects() == 0 ? 0 : round(a.gpSum() / a.subjects());
            rows.add(new ExamResultRow(a.studentId(), names.getOrDefault(a.studentId(), "?"),
                    a.subjects(), a.max(), a.obt(), round(pct), gpa,
                    GradeScale.letter(pct), GradeScale.isPass(pct), pos));
        }
        return new ExamResultSheet(exam.getId(), exam.getName(), rows);
    }

    // ---- Student marks history ----

    @Transactional(readOnly = true)
    public StudentMarksReport studentMarks(UUID studentId) {
        Student student = studentRepository.findById(studentId).map(TenantGuard::owned)
                .orElseThrow(() -> new NotFoundException("Student not found"));
        Map<UUID, String> examNames = examRepository.findAll().stream()
                .collect(Collectors.toMap(Exam::getId, Exam::getName));

        Map<UUID, List<Mark>> byExam = markRepository.findByStudentId(studentId).stream()
                .collect(Collectors.groupingBy(Mark::getExamId, LinkedHashMap::new, Collectors.toList()));

        List<StudentMarkRow> rows = new ArrayList<>();
        for (var e : byExam.entrySet()) {
            BigDecimal max = BigDecimal.ZERO, obt = BigDecimal.ZERO;
            for (Mark m : e.getValue()) {
                max = max.add(m.getMaxMarks());
                obt = obt.add(m.getObtainedMarks());
            }
            double pct = max.signum() == 0 ? 0 : percent(obt, max);
            rows.add(new StudentMarkRow(e.getKey(), examNames.getOrDefault(e.getKey(), "?"),
                    max, obt, round(pct), GradeScale.letter(pct), GradeScale.isPass(pct)));
        }
        return new StudentMarksReport(student.getId(), student.getFullName(), rows);
    }

    private static double percent(BigDecimal obtained, BigDecimal max) {
        return obtained.multiply(BigDecimal.valueOf(100))
                .divide(max, 4, RoundingMode.HALF_UP).doubleValue();
    }

    private static double round(double v) {
        return BigDecimal.valueOf(v).setScale(2, RoundingMode.HALF_UP).doubleValue();
    }
}
