package com.ims.people;

import com.ims.common.BadRequestException;
import com.ims.common.NotFoundException;
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
import com.ims.tenant.TenantGuard;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.UUID;

@Service
public class PeopleService {

    private final StudentRepository studentRepository;
    private final TeacherRepository teacherRepository;
    private final GuardianRepository guardianRepository;
    private final StudentGuardianRepository studentGuardianRepository;

    public PeopleService(StudentRepository studentRepository,
                         TeacherRepository teacherRepository,
                         GuardianRepository guardianRepository,
                         StudentGuardianRepository studentGuardianRepository) {
        this.studentRepository = studentRepository;
        this.teacherRepository = teacherRepository;
        this.guardianRepository = guardianRepository;
        this.studentGuardianRepository = studentGuardianRepository;
    }

    // ---- Student ----

    @Transactional
    public StudentResponse createStudent(CreateStudent req) {
        if (StringUtils.hasText(req.regNo()) && studentRepository.existsByRegNoIgnoreCase(req.regNo())) {
            throw new BadRequestException("Registration number already exists: " + req.regNo());
        }
        Student s = new Student();
        s.setFullName(req.fullName());
        s.setRegNo(req.regNo());
        s.setRollNo(req.rollNo());
        s.setDob(req.dob());
        s.setGender(req.gender());
        s.setPhotoUrl(req.photoUrl());
        s.setPhone(req.phone());
        s.setEmail(req.email());
        s.setAddress(req.address());
        return StudentResponse.from(studentRepository.save(s));
    }

    @Transactional(readOnly = true)
    public PageResponse<StudentResponse> listStudents(String q, Pageable pageable) {
        Page<Student> page = StringUtils.hasText(q)
                ? studentRepository.findByFullNameContainingIgnoreCase(q, pageable)
                : studentRepository.findAll(pageable);
        return PageResponse.from(page, StudentResponse::from);
    }

    @Transactional(readOnly = true)
    public StudentResponse getStudent(UUID id) {
        return StudentResponse.from(requireStudent(id));
    }

    @Transactional
    public StudentResponse updateStudent(UUID id, UpdateStudent req) {
        Student s = requireStudent(id);
        if (req.fullName() != null) s.setFullName(req.fullName());
        if (req.rollNo() != null) s.setRollNo(req.rollNo());
        if (req.dob() != null) s.setDob(req.dob());
        if (req.gender() != null) s.setGender(req.gender());
        if (req.photoUrl() != null) s.setPhotoUrl(req.photoUrl());
        if (req.phone() != null) s.setPhone(req.phone());
        if (req.email() != null) s.setEmail(req.email());
        if (req.address() != null) s.setAddress(req.address());
        if (req.status() != null) s.setStatus(req.status());
        return StudentResponse.from(s);
    }

    private Student requireStudent(UUID id) {
        return studentRepository.findById(id).map(TenantGuard::owned)
                .orElseThrow(() -> new NotFoundException("Student not found"));
    }

    // ---- Teacher ----

    @Transactional
    public TeacherResponse createTeacher(CreateTeacher req) {
        Teacher t = new Teacher();
        t.setFullName(req.fullName());
        t.setDob(req.dob());
        t.setGender(req.gender());
        if (req.designation() != null) t.setDesignation(req.designation());
        t.setPhone(req.phone());
        t.setEmail(req.email());
        t.setAddress(req.address());
        t.setJoinDate(req.joinDate());
        return TeacherResponse.from(teacherRepository.save(t));
    }

    @Transactional(readOnly = true)
    public PageResponse<TeacherResponse> listTeachers(String q, Pageable pageable) {
        Page<Teacher> page = StringUtils.hasText(q)
                ? teacherRepository.findByFullNameContainingIgnoreCase(q, pageable)
                : teacherRepository.findAll(pageable);
        return PageResponse.from(page, TeacherResponse::from);
    }

    @Transactional(readOnly = true)
    public TeacherResponse getTeacher(UUID id) {
        return TeacherResponse.from(requireTeacher(id));
    }

    @Transactional
    public TeacherResponse updateTeacher(UUID id, UpdateTeacher req) {
        Teacher t = requireTeacher(id);
        if (req.fullName() != null) t.setFullName(req.fullName());
        if (req.dob() != null) t.setDob(req.dob());
        if (req.gender() != null) t.setGender(req.gender());
        if (req.designation() != null) t.setDesignation(req.designation());
        if (req.phone() != null) t.setPhone(req.phone());
        if (req.email() != null) t.setEmail(req.email());
        if (req.address() != null) t.setAddress(req.address());
        if (req.joinDate() != null) t.setJoinDate(req.joinDate());
        if (req.status() != null) t.setStatus(req.status());
        return TeacherResponse.from(t);
    }

    private Teacher requireTeacher(UUID id) {
        return teacherRepository.findById(id).map(TenantGuard::owned)
                .orElseThrow(() -> new NotFoundException("Teacher not found"));
    }

    // ---- Guardian ----

    @Transactional
    public GuardianResponse createGuardian(CreateGuardian req) {
        Guardian g = new Guardian();
        g.setFullName(req.fullName());
        g.setPhone(req.phone());
        g.setEmail(req.email());
        g.setOccupation(req.occupation());
        g.setAddress(req.address());
        return GuardianResponse.from(guardianRepository.save(g));
    }

    @Transactional(readOnly = true)
    public PageResponse<GuardianResponse> listGuardians(String q, Pageable pageable) {
        Page<Guardian> page = StringUtils.hasText(q)
                ? guardianRepository.findByFullNameContainingIgnoreCase(q, pageable)
                : guardianRepository.findAll(pageable);
        return PageResponse.from(page, GuardianResponse::from);
    }

    @Transactional
    public GuardianResponse updateGuardian(UUID id, UpdateGuardian req) {
        Guardian g = guardianRepository.findById(id).map(TenantGuard::owned)
                .orElseThrow(() -> new NotFoundException("Guardian not found"));
        if (req.fullName() != null) g.setFullName(req.fullName());
        if (req.phone() != null) g.setPhone(req.phone());
        if (req.email() != null) g.setEmail(req.email());
        if (req.occupation() != null) g.setOccupation(req.occupation());
        if (req.address() != null) g.setAddress(req.address());
        return GuardianResponse.from(g);
    }

    // ---- Student-Guardian links ----

    @Transactional
    public StudentGuardianResponse linkGuardian(UUID studentId, LinkGuardian req) {
        requireStudent(studentId);
        guardianRepository.findById(req.guardianId()).map(TenantGuard::owned)
                .orElseThrow(() -> new BadRequestException("Guardian not found: " + req.guardianId()));
        if (studentGuardianRepository.existsByStudentIdAndGuardianId(studentId, req.guardianId())) {
            throw new BadRequestException("Guardian already linked to this student");
        }
        StudentGuardian sg = new StudentGuardian();
        sg.setStudentId(studentId);
        sg.setGuardianId(req.guardianId());
        sg.setRelation(req.relation());
        sg.setPrimary(req.primary());
        return StudentGuardianResponse.from(studentGuardianRepository.save(sg));
    }

    @Transactional(readOnly = true)
    public List<StudentGuardianResponse> listGuardiansOfStudent(UUID studentId) {
        return studentGuardianRepository.findByStudentId(studentId).stream()
                .map(StudentGuardianResponse::from).toList();
    }

    @Transactional
    public void unlinkGuardian(UUID linkId) {
        StudentGuardian sg = studentGuardianRepository.findById(linkId).map(TenantGuard::owned)
                .orElseThrow(() -> new NotFoundException("Link not found"));
        studentGuardianRepository.delete(sg);
    }
}
