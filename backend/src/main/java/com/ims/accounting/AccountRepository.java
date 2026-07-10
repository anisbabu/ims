package com.ims.accounting;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface AccountRepository extends JpaRepository<Account, UUID> {
    List<Account> findByType(AccountType type);
    Optional<Account> findBySystemKey(String systemKey);
    boolean existsByInstituteId(UUID instituteId);
}
