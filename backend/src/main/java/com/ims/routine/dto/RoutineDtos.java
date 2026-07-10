package com.ims.routine.dto;

import com.ims.routine.RoutineKind;
import com.ims.routine.RoutineSlot;
import jakarta.validation.constraints.NotNull;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.UUID;

public final class RoutineDtos {

    private RoutineDtos() {
    }

    public record CreateSlot(
            @NotNull RoutineKind kind,
            UUID sectionId,
            UUID gradeId,
            UUID subjectId,
            UUID teacherId,
            UUID examId,
            DayOfWeek dayOfWeek,
            LocalDate slotDate,
            @NotNull LocalTime startTime,
            @NotNull LocalTime endTime,
            String venue,
            String label) {
    }

    public record UpdateSlot(
            UUID sectionId,
            UUID gradeId,
            UUID subjectId,
            UUID teacherId,
            UUID examId,
            DayOfWeek dayOfWeek,
            LocalDate slotDate,
            LocalTime startTime,
            LocalTime endTime,
            String venue,
            String label) {
    }

    public record SlotResponse(
            UUID id, RoutineKind kind, UUID sectionId, UUID gradeId, UUID subjectId,
            UUID teacherId, UUID examId, DayOfWeek dayOfWeek, LocalDate slotDate,
            LocalTime startTime, LocalTime endTime, String venue, String label) {
        public static SlotResponse from(RoutineSlot s) {
            return new SlotResponse(s.getId(), s.getKind(), s.getSectionId(), s.getGradeId(),
                    s.getSubjectId(), s.getTeacherId(), s.getExamId(), s.getDayOfWeek(),
                    s.getSlotDate(), s.getStartTime(), s.getEndTime(), s.getVenue(), s.getLabel());
        }
    }
}
