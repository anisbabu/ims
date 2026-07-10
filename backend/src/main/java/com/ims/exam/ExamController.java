package com.ims.exam;

import com.ims.common.PageResponse;
import com.ims.exam.dto.ExamDtos.CreateExam;
import com.ims.exam.dto.ExamDtos.CreateExamType;
import com.ims.exam.dto.ExamDtos.CreateSubject;
import com.ims.exam.dto.ExamDtos.ExamResponse;
import com.ims.exam.dto.ExamDtos.ExamTypeResponse;
import com.ims.exam.dto.ExamDtos.MarkResponse;
import com.ims.exam.dto.ExamDtos.Marksheet;
import com.ims.exam.dto.ExamDtos.SaveMarks;
import com.ims.exam.dto.ExamDtos.SubjectResponse;
import com.ims.exam.dto.ExamDtos.UpdateExamStatus;
import jakarta.validation.Valid;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api")
public class ExamController {

    private static final String STAFF = "hasAnyRole('SUPER_ADMIN','INSTITUTE_ADMIN','TEACHER')";

    private final ExamService examService;

    public ExamController(ExamService examService) {
        this.examService = examService;
    }

    // Subjects
    @PostMapping("/subjects")
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize(STAFF)
    public SubjectResponse createSubject(@Valid @RequestBody CreateSubject req) {
        return examService.createSubject(req);
    }

    @GetMapping("/subjects")
    public List<SubjectResponse> listSubjects() {
        return examService.listSubjects();
    }

    // Exam types
    @PostMapping("/exam-types")
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize(STAFF)
    public ExamTypeResponse createExamType(@Valid @RequestBody CreateExamType req) {
        return examService.createExamType(req);
    }

    @GetMapping("/exam-types")
    public List<ExamTypeResponse> listExamTypes() {
        return examService.listExamTypes();
    }

    // Exams
    @PostMapping("/exams")
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize(STAFF)
    public ExamResponse createExam(@Valid @RequestBody CreateExam req) {
        return examService.createExam(req);
    }

    @GetMapping("/exams")
    public PageResponse<ExamResponse> listExams(@RequestParam(required = false) UUID academicYearId,
                                                Pageable pageable) {
        return examService.listExams(academicYearId, pageable);
    }

    @PatchMapping("/exams/{id}/status")
    @PreAuthorize(STAFF)
    public ExamResponse updateStatus(@PathVariable UUID id, @Valid @RequestBody UpdateExamStatus req) {
        return examService.updateStatus(id, req.status());
    }

    // Marks
    @PostMapping("/exams/{examId}/marks")
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize(STAFF)
    public List<MarkResponse> saveMarks(@PathVariable UUID examId, @Valid @RequestBody SaveMarks req) {
        return examService.saveMarks(examId, req);
    }

    @GetMapping("/exams/{examId}/marks")
    public List<MarkResponse> listMarks(@PathVariable UUID examId,
                                        @RequestParam(required = false) UUID studentId) {
        return examService.listMarks(examId, studentId);
    }

    // Marksheet (result)
    @GetMapping("/exams/{examId}/students/{studentId}/marksheet")
    public Marksheet marksheet(@PathVariable UUID examId, @PathVariable UUID studentId) {
        return examService.marksheet(examId, studentId);
    }
}
