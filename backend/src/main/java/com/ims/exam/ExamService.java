package com.ims.exam;

import com.ims.academic.AcademicYearRepository;
import com.ims.academic.GradeRepository;
import com.ims.common.BadRequestException;
import com.ims.common.NotFoundException;
import com.ims.common.PageResponse;
import com.ims.exam.dto.ExamDtos.CreateExam;
import com.ims.exam.dto.ExamDtos.CreateExamType;
import com.ims.exam.dto.ExamDtos.CreateSubject;
import com.ims.exam.dto.ExamDtos.ExamResponse;
import com.ims.exam.dto.ExamDtos.ExamTypeResponse;
import com.ims.exam.dto.ExamDtos.MarkEntry;
import com.ims.exam.dto.ExamDtos.MarkResponse;
import com.ims.exam.dto.ExamDtos.Marksheet;
import com.ims.exam.dto.ExamDtos.MarksheetLine;
import com.ims.exam.dto.ExamDtos.SaveMarks;
import com.ims.exam.dto.ExamDtos.SubjectResponse;
import com.ims.people.Student;
import com.ims.people.StudentRepository;
import com.ims.tenant.TenantGuard;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class ExamService {

    private final SubjectRepository subjectRepository;
    private final ExamTypeRepository examTypeRepository;
    private final ExamRepository examRepository;
    private final MarkRepository markRepository;
    private final StudentRepository studentRepository;
    private final AcademicYearRepository academicYearRepository;
    private final GradeRepository gradeRepository;

    public ExamService(SubjectRepository subjectRepository,
                       ExamTypeRepository examTypeRepository,
                       ExamRepository examRepository,
                       MarkRepository markRepository,
                       StudentRepository studentRepository,
                       AcademicYearRepository academicYearRepository,
                       GradeRepository gradeRepository) {
        this.subjectRepository = subjectRepository;
        this.examTypeRepository = examTypeRepository;
        this.examRepository = examRepository;
        this.markRepository = markRepository;
        this.studentRepository = studentRepository;
        this.academicYearRepository = academicYearRepository;
        this.gradeRepository = gradeRepository;
    }

    // ---- Subjects ----

    @Transactional
    public SubjectResponse createSubject(CreateSubject req) {
        Subject s = new Subject();
        s.setName(req.name());
        s.setCode(req.code());
        return SubjectResponse.from(subjectRepository.save(s));
    }

    @Transactional(readOnly = true)
    public List<SubjectResponse> listSubjects() {
        return subjectRepository.findAll().stream()
                .sorted(Comparator.comparing(Subject::getName))
                .map(SubjectResponse::from).toList();
    }

    // ---- Exam types ----

    @Transactional
    public ExamTypeResponse createExamType(CreateExamType req) {
        ExamType t = new ExamType();
        t.setName(req.name());
        t.setWeightPercent(req.weightPercent());
        return ExamTypeResponse.from(examTypeRepository.save(t));
    }

    @Transactional(readOnly = true)
    public List<ExamTypeResponse> listExamTypes() {
        return examTypeRepository.findAll().stream()
                .sorted(Comparator.comparing(ExamType::getName))
                .map(ExamTypeResponse::from).toList();
    }

    // ---- Exams ----

    @Transactional
    public ExamResponse createExam(CreateExam req) {
        examTypeRepository.findById(req.examTypeId()).map(TenantGuard::owned)
                .orElseThrow(() -> new BadRequestException("Exam type not found"));
        academicYearRepository.findById(req.academicYearId()).map(TenantGuard::owned)
                .orElseThrow(() -> new BadRequestException("Academic year not found"));
        if (req.gradeId() != null) {
            gradeRepository.findById(req.gradeId()).map(TenantGuard::owned)
                    .orElseThrow(() -> new BadRequestException("Grade not found"));
        }
        Exam e = new Exam();
        e.setName(req.name());
        e.setExamTypeId(req.examTypeId());
        e.setAcademicYearId(req.academicYearId());
        e.setGradeId(req.gradeId());
        e.setStartDate(req.startDate());
        e.setEndDate(req.endDate());
        e.setStatus(ExamStatus.SCHEDULED);
        return ExamResponse.from(examRepository.save(e));
    }

    @Transactional(readOnly = true)
    public PageResponse<ExamResponse> listExams(UUID academicYearId, Pageable pageable) {
        var page = (academicYearId != null)
                ? examRepository.findByAcademicYearId(academicYearId, pageable)
                : examRepository.findAll(pageable);
        return PageResponse.from(page, ExamResponse::from);
    }

    @Transactional
    public ExamResponse updateStatus(UUID examId, ExamStatus status) {
        Exam e = requireExam(examId);
        e.setStatus(status);
        return ExamResponse.from(e);
    }

    // ---- Marks ----

    @Transactional
    public List<MarkResponse> saveMarks(UUID examId, SaveMarks req) {
        requireExam(examId);
        studentRepository.findById(req.studentId()).map(TenantGuard::owned)
                .orElseThrow(() -> new BadRequestException("Student not found"));
        List<MarkResponse> saved = new ArrayList<>();
        for (MarkEntry entry : req.entries()) {
            subjectRepository.findById(entry.subjectId()).map(TenantGuard::owned)
                    .orElseThrow(() -> new BadRequestException("Subject not found: " + entry.subjectId()));
            if (entry.obtainedMarks().compareTo(entry.maxMarks()) > 0) {
                throw new BadRequestException("Obtained marks exceed max for subject " + entry.subjectId());
            }
            Mark mark = markRepository
                    .findByExamIdAndStudentIdAndSubjectId(examId, req.studentId(), entry.subjectId())
                    .orElseGet(Mark::new);
            mark.setExamId(examId);
            mark.setStudentId(req.studentId());
            mark.setSubjectId(entry.subjectId());
            mark.setMaxMarks(entry.maxMarks());
            mark.setObtainedMarks(entry.obtainedMarks());
            mark.setRemarks(entry.remarks());
            saved.add(MarkResponse.from(markRepository.save(mark)));
        }
        return saved;
    }

    @Transactional(readOnly = true)
    public List<MarkResponse> listMarks(UUID examId, UUID studentId) {
        requireExam(examId);
        List<Mark> marks = (studentId != null)
                ? markRepository.findByExamIdAndStudentId(examId, studentId)
                : markRepository.findByExamId(examId);
        return marks.stream().map(MarkResponse::from).toList();
    }

    // ---- Marksheet (computed result) ----

    @Transactional(readOnly = true)
    public Marksheet marksheet(UUID examId, UUID studentId) {
        Exam exam = requireExam(examId);
        Student student = studentRepository.findById(studentId).map(TenantGuard::owned)
                .orElseThrow(() -> new NotFoundException("Student not found"));

        List<Mark> marks = markRepository.findByExamIdAndStudentId(examId, studentId);
        if (marks.isEmpty()) {
            throw new NotFoundException("No marks recorded for this student in this exam");
        }

        Map<UUID, String> subjectNames = subjectRepository.findAll().stream()
                .collect(Collectors.toMap(Subject::getId, Subject::getName));

        List<MarksheetLine> lines = new ArrayList<>();
        BigDecimal totalMax = BigDecimal.ZERO;
        BigDecimal totalObtained = BigDecimal.ZERO;
        double gpSum = 0;
        boolean overallPass = true;

        for (Mark m : marks) {
            double pct = percent(m.getObtainedMarks(), m.getMaxMarks());
            boolean pass = GradeScale.isPass(pct);
            if (!pass) overallPass = false;
            gpSum += GradeScale.gradePoint(pct);
            totalMax = totalMax.add(m.getMaxMarks());
            totalObtained = totalObtained.add(m.getObtainedMarks());
            lines.add(new MarksheetLine(m.getSubjectId(),
                    subjectNames.getOrDefault(m.getSubjectId(), "?"),
                    m.getMaxMarks(), m.getObtainedMarks(),
                    round(pct), GradeScale.letter(pct), pass));
        }

        double overallPercent = totalMax.signum() == 0 ? 0
                : percent(totalObtained, totalMax);
        double gpa = round(gpSum / marks.size());

        return new Marksheet(exam.getId(), exam.getName(), student.getId(), student.getFullName(),
                lines, totalMax, totalObtained, round(overallPercent),
                gpa, GradeScale.letter(overallPercent), overallPass,
                position(examId, studentId));
    }

    /** 1-based rank of the student among all students who sat this exam, by overall percent. */
    private Integer position(UUID examId, UUID studentId) {
        Map<UUID, List<Mark>> byStudent = markRepository.findByExamId(examId).stream()
                .collect(Collectors.groupingBy(Mark::getStudentId));
        List<Map.Entry<UUID, Double>> ranked = byStudent.entrySet().stream()
                .map(e -> Map.entry(e.getKey(), overallPercentOf(e.getValue())))
                .sorted(Map.Entry.<UUID, Double>comparingByValue().reversed())
                .toList();
        for (int i = 0; i < ranked.size(); i++) {
            if (ranked.get(i).getKey().equals(studentId)) {
                return i + 1;
            }
        }
        return null;
    }

    private double overallPercentOf(List<Mark> marks) {
        BigDecimal max = BigDecimal.ZERO;
        BigDecimal obt = BigDecimal.ZERO;
        for (Mark m : marks) {
            max = max.add(m.getMaxMarks());
            obt = obt.add(m.getObtainedMarks());
        }
        return max.signum() == 0 ? 0 : percent(obt, max);
    }

    private static double percent(BigDecimal obtained, BigDecimal max) {
        return obtained.multiply(BigDecimal.valueOf(100))
                .divide(max, 4, RoundingMode.HALF_UP).doubleValue();
    }

    private static double round(double v) {
        return BigDecimal.valueOf(v).setScale(2, RoundingMode.HALF_UP).doubleValue();
    }

    private Exam requireExam(UUID id) {
        return examRepository.findById(id).map(TenantGuard::owned)
                .orElseThrow(() -> new NotFoundException("Exam not found"));
    }
}
