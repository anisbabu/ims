package com.ims.people;

import com.ims.common.PageResponse;
import com.ims.people.dto.PeopleDtos.CreateGuardian;
import com.ims.people.dto.PeopleDtos.CreateStudent;
import com.ims.people.dto.PeopleDtos.CreateTeacher;
import com.ims.people.dto.PeopleDtos.GuardianResponse;
import com.ims.people.dto.PeopleDtos.LinkGuardian;
import com.ims.people.dto.PeopleDtos.StudentGuardianResponse;
import com.ims.people.dto.PeopleDtos.StudentResponse;
import com.ims.people.dto.PeopleDtos.TeacherResponse;
import com.ims.people.dto.PeopleDtos.UpdateGuardian;
import com.ims.people.dto.PeopleDtos.UpdateStudent;
import com.ims.people.dto.PeopleDtos.UpdateTeacher;
import jakarta.validation.Valid;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
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
public class PeopleController {

    private static final String ADMIN = "hasAnyRole('SUPER_ADMIN','INSTITUTE_ADMIN')";

    private final PeopleService peopleService;

    public PeopleController(PeopleService peopleService) {
        this.peopleService = peopleService;
    }

    // ---- Students ----
    @PostMapping("/students")
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize(ADMIN)
    public StudentResponse createStudent(@Valid @RequestBody CreateStudent req) {
        return peopleService.createStudent(req);
    }

    @GetMapping("/students")
    public PageResponse<StudentResponse> listStudents(@RequestParam(required = false) String q, Pageable pageable) {
        return peopleService.listStudents(q, pageable);
    }

    @GetMapping("/students/{id}")
    public StudentResponse getStudent(@PathVariable UUID id) {
        return peopleService.getStudent(id);
    }

    @PutMapping("/students/{id}")
    @PreAuthorize(ADMIN)
    public StudentResponse updateStudent(@PathVariable UUID id, @Valid @RequestBody UpdateStudent req) {
        return peopleService.updateStudent(id, req);
    }

    @DeleteMapping("/students/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize(ADMIN)
    public void deleteStudent(@PathVariable UUID id) {
        peopleService.deleteStudent(id);
    }

    // ---- Student <-> Guardian links ----
    @PostMapping("/students/{studentId}/guardians")
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize(ADMIN)
    public StudentGuardianResponse linkGuardian(@PathVariable UUID studentId, @Valid @RequestBody LinkGuardian req) {
        return peopleService.linkGuardian(studentId, req);
    }

    @GetMapping("/students/{studentId}/guardians")
    public List<StudentGuardianResponse> listGuardiansOfStudent(@PathVariable UUID studentId) {
        return peopleService.listGuardiansOfStudent(studentId);
    }

    @DeleteMapping("/student-guardians/{linkId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize(ADMIN)
    public void unlinkGuardian(@PathVariable UUID linkId) {
        peopleService.unlinkGuardian(linkId);
    }

    // ---- Teachers ----
    @PostMapping("/teachers")
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize(ADMIN)
    public TeacherResponse createTeacher(@Valid @RequestBody CreateTeacher req) {
        return peopleService.createTeacher(req);
    }

    @GetMapping("/teachers")
    public PageResponse<TeacherResponse> listTeachers(@RequestParam(required = false) String q, Pageable pageable) {
        return peopleService.listTeachers(q, pageable);
    }

    @GetMapping("/teachers/{id}")
    public TeacherResponse getTeacher(@PathVariable UUID id) {
        return peopleService.getTeacher(id);
    }

    @PutMapping("/teachers/{id}")
    @PreAuthorize(ADMIN)
    public TeacherResponse updateTeacher(@PathVariable UUID id, @Valid @RequestBody UpdateTeacher req) {
        return peopleService.updateTeacher(id, req);
    }

    @DeleteMapping("/teachers/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize(ADMIN)
    public void deleteTeacher(@PathVariable UUID id) {
        peopleService.deleteTeacher(id);
    }

    // ---- Guardians ----
    @PostMapping("/guardians")
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize(ADMIN)
    public GuardianResponse createGuardian(@Valid @RequestBody CreateGuardian req) {
        return peopleService.createGuardian(req);
    }

    @GetMapping("/guardians")
    public PageResponse<GuardianResponse> listGuardians(@RequestParam(required = false) String q, Pageable pageable) {
        return peopleService.listGuardians(q, pageable);
    }

    @GetMapping("/guardians/{id}")
    public GuardianResponse getGuardian(@PathVariable UUID id) {
        return peopleService.getGuardian(id);
    }

    @PutMapping("/guardians/{id}")
    @PreAuthorize(ADMIN)
    public GuardianResponse updateGuardian(@PathVariable UUID id, @Valid @RequestBody UpdateGuardian req) {
        return peopleService.updateGuardian(id, req);
    }

    @DeleteMapping("/guardians/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize(ADMIN)
    public void deleteGuardian(@PathVariable UUID id) {
        peopleService.deleteGuardian(id);
    }
}
