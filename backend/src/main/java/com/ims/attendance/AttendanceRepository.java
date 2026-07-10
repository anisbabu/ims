package com.ims.attendance;

import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface AttendanceRepository extends JpaRepository<Attendance, UUID> {
    List<Attendance> findByDate(LocalDate date);
    List<Attendance> findByDateAndSectionId(LocalDate date, UUID sectionId);
    List<Attendance> findByStudentIdAndDateBetween(UUID studentId, LocalDate from, LocalDate to);
    Optional<Attendance> findByStudentIdAndDate(UUID studentId, LocalDate date);
}
