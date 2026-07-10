package com.ims.people;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface TeacherRepository extends JpaRepository<Teacher, UUID> {
    Page<Teacher> findByFullNameContainingIgnoreCase(String q, Pageable pageable);
}
