package com.ims.people;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface StudentGuardianRepository extends JpaRepository<StudentGuardian, UUID> {
    List<StudentGuardian> findByStudentId(UUID studentId);
    List<StudentGuardian> findByGuardianId(UUID guardianId);
    boolean existsByStudentIdAndGuardianId(UUID studentId, UUID guardianId);
    void deleteByStudentId(UUID studentId);
    void deleteByGuardianId(UUID guardianId);
}
