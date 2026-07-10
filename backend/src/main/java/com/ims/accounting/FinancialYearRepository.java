package com.ims.accounting;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface FinancialYearRepository extends JpaRepository<FinancialYear, UUID> {
    List<FinancialYear> findByCurrentTrue();
    boolean existsByInstituteId(UUID instituteId);
}
