package com.ims.routine;

import com.ims.tenant.TenantAwareEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.Filter;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.UUID;

/**
 * A single entry in a timetable. CLASS slots recur weekly on {@code dayOfWeek};
 * EXAM slots occur on a specific {@code slotDate} and reference an exam.
 */
@Getter
@Setter
@Entity
@Table(name = "routine_slot")
@Filter(name = TenantAwareEntity.TENANT_FILTER, condition = TenantAwareEntity.TENANT_CONDITION)
public class RoutineSlot extends TenantAwareEntity {

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 8)
    private RoutineKind kind;

    @Column(name = "section_id")
    private UUID sectionId;

    @Column(name = "grade_id")
    private UUID gradeId;

    @Column(name = "subject_id")
    private UUID subjectId;

    @Column(name = "teacher_id")
    private UUID teacherId;

    @Column(name = "exam_id")
    private UUID examId;

    /** CLASS routine: recurring weekday. */
    @Enumerated(EnumType.STRING)
    @Column(name = "day_of_week", length = 12)
    private DayOfWeek dayOfWeek;

    /** EXAM routine: specific date. */
    @Column(name = "slot_date")
    private LocalDate slotDate;

    @Column(name = "start_time", nullable = false)
    private LocalTime startTime;

    @Column(name = "end_time", nullable = false)
    private LocalTime endTime;

    @Column(length = 128)
    private String venue;

    @Column(length = 128)
    private String label;
}
