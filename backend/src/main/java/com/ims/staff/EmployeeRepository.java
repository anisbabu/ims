package com.ims.staff;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface EmployeeRepository extends JpaRepository<Employee, UUID> {
    Page<Employee> findByFullNameContainingIgnoreCase(String q, Pageable pageable);
}
