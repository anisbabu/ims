package com.ims.people;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface StudentRepository extends JpaRepository<Student, UUID> {
    Page<Student> findByFullNameContainingIgnoreCase(String q, Pageable pageable);
    boolean existsByRegNoIgnoreCase(String regNo);
}
