package com.ims.institute;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface InstituteRepository extends JpaRepository<Institute, UUID> {
    boolean existsByCodeIgnoreCase(String code);
    Optional<Institute> findByCodeIgnoreCase(String code);
}
