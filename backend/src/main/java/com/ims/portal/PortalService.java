package com.ims.portal;

import com.ims.academic.AcademicYearRepository;
import com.ims.academic.GradeRepository;
import com.ims.academic.Section;
import com.ims.academic.SectionRepository;
import com.ims.admission.Admission;
import com.ims.admission.AdmissionRepository;
import com.ims.admission.AdmissionStatus;
import com.ims.attendance.AttendanceService;
import com.ims.auth.SecurityUser;
import com.ims.auth.User;
import com.ims.auth.UserRepository;
import com.ims.common.BadRequestException;
import com.ims.common.NotFoundException;
import com.ims.fee.FeeService;
import com.ims.people.PeopleService;
import com.ims.people.StudentGuardian;
import com.ims.people.StudentGuardianRepository;
import com.ims.portal.dto.PortalDtos.AdmissionInfo;
import com.ims.portal.dto.PortalDtos.ChildSummary;
import com.ims.portal.dto.PortalDtos.GuardianPortal;
import com.ims.portal.dto.PortalDtos.SectionInfo;
import com.ims.portal.dto.PortalDtos.StudentPortal;
import com.ims.portal.dto.PortalDtos.TeacherPortal;
import com.ims.report.AcademicReportService;
import com.ims.routine.RoutineKind;
import com.ims.routine.RoutineSlotRepository;
import com.ims.routine.dto.RoutineDtos.SlotResponse;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;

/**
 * Read-only aggregates for the role-scoped portals. Scope is always derived
 * server-side from the caller's linked profile (User.profileId) — portal
 * endpoints never accept a student/teacher/guardian id from the client.
 */
@Service
public class PortalService {

    private final UserRepository userRepository;
    private final PeopleService peopleService;
    private final StudentGuardianRepository studentGuardianRepository;
    private final AdmissionRepository admissionRepository;
    private final AcademicYearRepository academicYearRepository;
    private final GradeRepository gradeRepository;
    private final SectionRepository sectionRepository;
    private final AttendanceService attendanceService;
    private final FeeService feeService;
    private final AcademicReportService academicReportService;
    private final RoutineSlotRepository routineSlotRepository;

    public PortalService(UserRepository userRepository,
                         PeopleService peopleService,
                         StudentGuardianRepository studentGuardianRepository,
                         AdmissionRepository admissionRepository,
                         AcademicYearRepository academicYearRepository,
                         GradeRepository gradeRepository,
                         SectionRepository sectionRepository,
                         AttendanceService attendanceService,
                         FeeService feeService,
                         AcademicReportService academicReportService,
                         RoutineSlotRepository routineSlotRepository) {
        this.userRepository = userRepository;
        this.peopleService = peopleService;
        this.studentGuardianRepository = studentGuardianRepository;
        this.admissionRepository = admissionRepository;
        this.academicYearRepository = academicYearRepository;
        this.gradeRepository = gradeRepository;
        this.sectionRepository = sectionRepository;
        this.attendanceService = attendanceService;
        this.feeService = feeService;
        this.academicReportService = academicReportService;
        this.routineSlotRepository = routineSlotRepository;
    }

    @Transactional(readOnly = true)
    public StudentPortal studentPortal(SecurityUser me) {
        UUID studentId = requireProfile(me, "student");
        var student = peopleService.getStudent(studentId);
        AdmissionInfo admission = admissionInfo(studentId);
        List<SlotResponse> routine = admission != null && admission.sectionId() != null
                ? routineSlotRepository.findByKindAndSectionId(RoutineKind.CLASS, admission.sectionId())
                        .stream()
                        .sorted(Comparator.comparing(s -> s.getStartTime()))
                        .map(SlotResponse::from).toList()
                : List.of();
        return new StudentPortal(
                student,
                admission,
                attendanceService.summary(studentId, LocalDate.now().minusDays(365), LocalDate.now()),
                feeService.summary(studentId),
                academicReportService.studentMarks(studentId),
                routine);
    }

    @Transactional(readOnly = true)
    public GuardianPortal guardianPortal(SecurityUser me) {
        UUID guardianId = requireProfile(me, "guardian");
        var guardian = peopleService.getGuardian(guardianId);
        List<ChildSummary> children = studentGuardianRepository.findByGuardianId(guardianId).stream()
                .map(StudentGuardian::getStudentId)
                .map(studentId -> {
                    var student = peopleService.getStudent(studentId);
                    return new ChildSummary(
                            studentId,
                            student.fullName(),
                            admissionInfo(studentId),
                            attendanceService.summary(studentId,
                                    LocalDate.now().minusDays(365), LocalDate.now()),
                            feeService.summary(studentId));
                })
                .toList();
        return new GuardianPortal(guardian, children);
    }

    @Transactional(readOnly = true)
    public TeacherPortal teacherPortal(SecurityUser me) {
        UUID teacherId = requireProfile(me, "teacher");
        var teacher = peopleService.getTeacher(teacherId);
        List<SectionInfo> sections = sectionRepository.findByClassTeacherId(teacherId).stream()
                .map(s -> new SectionInfo(s.getId(), s.getName(), gradeName(s)))
                .toList();
        List<SlotResponse> routine = routineSlotRepository.findByTeacherId(teacherId).stream()
                .sorted(Comparator.comparing(s -> s.getStartTime()))
                .map(SlotResponse::from).toList();
        return new TeacherPortal(teacher, sections, routine);
    }

    private UUID requireProfile(SecurityUser me, String kind) {
        User user = userRepository.findById(me.getId())
                .orElseThrow(() -> new NotFoundException("User not found"));
        if (user.getProfileId() == null) {
            throw new BadRequestException("No " + kind + " profile is linked to this account");
        }
        return user.getProfileId();
    }

    /** Latest enrollment for the student — prefers ENROLLED, else the most recent. */
    private AdmissionInfo admissionInfo(UUID studentId) {
        List<Admission> admissions = admissionRepository.findByStudentId(studentId);
        Admission a = admissions.stream()
                .filter(x -> x.getStatus() == AdmissionStatus.ENROLLED)
                .max(Comparator.comparing(Admission::getCreatedAt))
                .or(() -> admissions.stream().max(Comparator.comparing(Admission::getCreatedAt)))
                .orElse(null);
        if (a == null) {
            return null;
        }
        String year = a.getAcademicYearId() != null
                ? academicYearRepository.findById(a.getAcademicYearId()).map(y -> y.getName()).orElse(null)
                : null;
        String grade = a.getGradeId() != null
                ? gradeRepository.findById(a.getGradeId()).map(g -> g.getName()).orElse(null)
                : null;
        String section = a.getSectionId() != null
                ? sectionRepository.findById(a.getSectionId()).map(Section::getName).orElse(null)
                : null;
        return new AdmissionInfo(a.getId(), a.getAcademicYearId(), year,
                a.getGradeId(), grade, a.getSectionId(), section, a.getStatus());
    }

    private String gradeName(Section s) {
        return gradeRepository.findById(s.getGradeId()).map(g -> g.getName()).orElse(null);
    }
}
