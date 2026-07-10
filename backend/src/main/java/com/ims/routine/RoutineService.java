package com.ims.routine;

import com.ims.academic.GradeRepository;
import com.ims.academic.SectionRepository;
import com.ims.common.BadRequestException;
import com.ims.common.NotFoundException;
import com.ims.exam.ExamRepository;
import com.ims.exam.SubjectRepository;
import com.ims.people.TeacherRepository;
import com.ims.routine.dto.RoutineDtos.CreateSlot;
import com.ims.routine.dto.RoutineDtos.SlotResponse;
import com.ims.routine.dto.RoutineDtos.UpdateSlot;
import com.ims.tenant.TenantGuard;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalTime;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;

@Service
public class RoutineService {

    private final RoutineSlotRepository routineRepository;
    private final SectionRepository sectionRepository;
    private final GradeRepository gradeRepository;
    private final SubjectRepository subjectRepository;
    private final TeacherRepository teacherRepository;
    private final ExamRepository examRepository;

    public RoutineService(RoutineSlotRepository routineRepository,
                          SectionRepository sectionRepository,
                          GradeRepository gradeRepository,
                          SubjectRepository subjectRepository,
                          TeacherRepository teacherRepository,
                          ExamRepository examRepository) {
        this.routineRepository = routineRepository;
        this.sectionRepository = sectionRepository;
        this.gradeRepository = gradeRepository;
        this.subjectRepository = subjectRepository;
        this.teacherRepository = teacherRepository;
        this.examRepository = examRepository;
    }

    @Transactional
    public SlotResponse create(CreateSlot req) {
        validateTimes(req.startTime(), req.endTime());
        validateKind(req.kind(), req.sectionId(), req.dayOfWeek(), req.examId(), req.slotDate());
        validateRefs(req.sectionId(), req.gradeId(), req.subjectId(), req.teacherId(), req.examId());

        RoutineSlot s = new RoutineSlot();
        s.setKind(req.kind());
        s.setSectionId(req.sectionId());
        s.setGradeId(req.gradeId());
        s.setSubjectId(req.subjectId());
        s.setTeacherId(req.teacherId());
        s.setExamId(req.examId());
        s.setDayOfWeek(req.dayOfWeek());
        s.setSlotDate(req.slotDate());
        s.setStartTime(req.startTime());
        s.setEndTime(req.endTime());
        s.setVenue(req.venue());
        s.setLabel(req.label());
        return SlotResponse.from(routineRepository.save(s));
    }

    @Transactional(readOnly = true)
    public List<SlotResponse> list(RoutineKind kind, UUID sectionId, UUID examId) {
        List<RoutineSlot> slots;
        if (kind == RoutineKind.CLASS && sectionId != null) {
            slots = routineRepository.findByKindAndSectionId(kind, sectionId);
        } else if (kind == RoutineKind.EXAM && examId != null) {
            slots = routineRepository.findByKindAndExamId(kind, examId);
        } else if (kind != null) {
            slots = routineRepository.findByKind(kind);
        } else {
            slots = routineRepository.findAll();
        }
        return slots.stream()
                .sorted(Comparator.comparing(RoutineSlot::getStartTime))
                .map(SlotResponse::from).toList();
    }

    @Transactional
    public SlotResponse update(UUID id, UpdateSlot req) {
        RoutineSlot s = require(id);
        if (req.sectionId() != null) s.setSectionId(req.sectionId());
        if (req.gradeId() != null) s.setGradeId(req.gradeId());
        if (req.subjectId() != null) s.setSubjectId(req.subjectId());
        if (req.teacherId() != null) s.setTeacherId(req.teacherId());
        if (req.examId() != null) s.setExamId(req.examId());
        if (req.dayOfWeek() != null) s.setDayOfWeek(req.dayOfWeek());
        if (req.slotDate() != null) s.setSlotDate(req.slotDate());
        if (req.startTime() != null) s.setStartTime(req.startTime());
        if (req.endTime() != null) s.setEndTime(req.endTime());
        if (req.venue() != null) s.setVenue(req.venue());
        if (req.label() != null) s.setLabel(req.label());
        validateTimes(s.getStartTime(), s.getEndTime());
        validateRefs(s.getSectionId(), s.getGradeId(), s.getSubjectId(), s.getTeacherId(), s.getExamId());
        return SlotResponse.from(s);
    }

    @Transactional
    public void delete(UUID id) {
        routineRepository.delete(require(id));
    }

    private void validateTimes(LocalTime start, LocalTime end) {
        if (start != null && end != null && !end.isAfter(start)) {
            throw new BadRequestException("End time must be after start time");
        }
    }

    private void validateKind(RoutineKind kind, UUID sectionId, java.time.DayOfWeek day,
                              UUID examId, java.time.LocalDate date) {
        if (kind == RoutineKind.CLASS) {
            if (sectionId == null || day == null) {
                throw new BadRequestException("CLASS routine requires sectionId and dayOfWeek");
            }
        } else if (kind == RoutineKind.EXAM) {
            if (examId == null || date == null) {
                throw new BadRequestException("EXAM routine requires examId and slotDate");
            }
        }
    }

    private void validateRefs(UUID sectionId, UUID gradeId, UUID subjectId, UUID teacherId, UUID examId) {
        if (sectionId != null) sectionRepository.findById(sectionId).map(TenantGuard::owned)
                .orElseThrow(() -> new BadRequestException("Section not found"));
        if (gradeId != null) gradeRepository.findById(gradeId).map(TenantGuard::owned)
                .orElseThrow(() -> new BadRequestException("Grade not found"));
        if (subjectId != null) subjectRepository.findById(subjectId).map(TenantGuard::owned)
                .orElseThrow(() -> new BadRequestException("Subject not found"));
        if (teacherId != null) teacherRepository.findById(teacherId).map(TenantGuard::owned)
                .orElseThrow(() -> new BadRequestException("Teacher not found"));
        if (examId != null) examRepository.findById(examId).map(TenantGuard::owned)
                .orElseThrow(() -> new BadRequestException("Exam not found"));
    }

    private RoutineSlot require(UUID id) {
        return routineRepository.findById(id).map(TenantGuard::owned)
                .orElseThrow(() -> new NotFoundException("Routine slot not found"));
    }
}
