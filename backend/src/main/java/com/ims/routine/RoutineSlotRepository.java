package com.ims.routine;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface RoutineSlotRepository extends JpaRepository<RoutineSlot, UUID> {
    List<RoutineSlot> findByKindAndSectionId(RoutineKind kind, UUID sectionId);
    List<RoutineSlot> findByKindAndExamId(RoutineKind kind, UUID examId);
    List<RoutineSlot> findByKind(RoutineKind kind);
}
