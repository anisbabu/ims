package com.ims.academic;

import com.ims.academic.dto.AcademicDtos.CreateGrade;
import com.ims.academic.dto.AcademicDtos.CreateSection;
import com.ims.academic.dto.AcademicDtos.CreateYear;
import com.ims.academic.dto.AcademicDtos.GradeResponse;
import com.ims.academic.dto.AcademicDtos.SectionResponse;
import com.ims.academic.dto.AcademicDtos.UpdateGrade;
import com.ims.academic.dto.AcademicDtos.UpdateSection;
import com.ims.academic.dto.AcademicDtos.UpdateYear;
import com.ims.academic.dto.AcademicDtos.YearResponse;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api")
@PreAuthorize("hasAnyRole('SUPER_ADMIN','INSTITUTE_ADMIN')")
public class AcademicController {

    private final AcademicService academicService;

    public AcademicController(AcademicService academicService) {
        this.academicService = academicService;
    }

    // Academic Years
    @PostMapping("/academic-years")
    @ResponseStatus(HttpStatus.CREATED)
    public YearResponse createYear(@Valid @RequestBody CreateYear req) {
        return academicService.createYear(req);
    }

    @GetMapping("/academic-years")
    @PreAuthorize("isAuthenticated()")
    public List<YearResponse> listYears() {
        return academicService.listYears();
    }

    @PutMapping("/academic-years/{id}")
    public YearResponse updateYear(@PathVariable UUID id, @Valid @RequestBody UpdateYear req) {
        return academicService.updateYear(id, req);
    }

    // Grades
    @PostMapping("/grades")
    @ResponseStatus(HttpStatus.CREATED)
    public GradeResponse createGrade(@Valid @RequestBody CreateGrade req) {
        return academicService.createGrade(req);
    }

    @GetMapping("/grades")
    @PreAuthorize("isAuthenticated()")
    public List<GradeResponse> listGrades() {
        return academicService.listGrades();
    }

    @PutMapping("/grades/{id}")
    public GradeResponse updateGrade(@PathVariable UUID id, @Valid @RequestBody UpdateGrade req) {
        return academicService.updateGrade(id, req);
    }

    // Sections
    @PostMapping("/sections")
    @ResponseStatus(HttpStatus.CREATED)
    public SectionResponse createSection(@Valid @RequestBody CreateSection req) {
        return academicService.createSection(req);
    }

    @GetMapping("/sections")
    @PreAuthorize("isAuthenticated()")
    public List<SectionResponse> listSections(@RequestParam(required = false) UUID gradeId) {
        return academicService.listSections(gradeId);
    }

    @PutMapping("/sections/{id}")
    public SectionResponse updateSection(@PathVariable UUID id, @Valid @RequestBody UpdateSection req) {
        return academicService.updateSection(id, req);
    }
}
