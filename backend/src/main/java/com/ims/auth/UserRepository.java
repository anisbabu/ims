package com.ims.auth;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface UserRepository extends JpaRepository<User, UUID> {
    Optional<User> findByEmailIgnoreCase(String email);
    boolean existsByEmailIgnoreCase(String email);

    Page<User> findByInstituteId(UUID instituteId, Pageable pageable);
    Page<User> findByInstituteIdAndRole(UUID instituteId, Role role, Pageable pageable);
    Page<User> findByRole(Role role, Pageable pageable);
}
