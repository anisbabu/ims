package com.ims.people;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface GuardianRepository extends JpaRepository<Guardian, UUID> {
    Page<Guardian> findByFullNameContainingIgnoreCase(String q, Pageable pageable);
}
