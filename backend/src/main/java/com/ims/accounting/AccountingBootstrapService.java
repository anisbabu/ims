package com.ims.accounting;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.Year;
import java.util.List;
import java.util.UUID;

/**
 * Seeds a default chart of accounts and a current financial year for an institute.
 * Called when an institute is created. Idempotent per institute.
 */
@Service
public class AccountingBootstrapService {

    private record Seed(String code, String name, AccountType type, String key) {
    }

    private static final List<Seed> DEFAULT_ACCOUNTS = List.of(
            new Seed("1000", "Cash", AccountType.ASSET, "CASH"),
            new Seed("1010", "Bank", AccountType.ASSET, "BANK"),
            new Seed("1200", "Accounts Receivable", AccountType.ASSET, "AR"),
            new Seed("2000", "Accounts Payable", AccountType.LIABILITY, "AP"),
            new Seed("3000", "Capital", AccountType.EQUITY, "CAPITAL"),
            new Seed("3900", "Retained Earnings", AccountType.EQUITY, "RETAINED_EARNINGS"),
            new Seed("4000", "Fee Income", AccountType.INCOME, "FEE_INCOME"),
            new Seed("4900", "Other Income", AccountType.INCOME, "OTHER_INCOME"),
            new Seed("5000", "Salaries", AccountType.EXPENSE, "SALARY"),
            new Seed("5100", "General Expense", AccountType.EXPENSE, "GENERAL_EXPENSE")
    );

    private final AccountRepository accountRepository;
    private final FinancialYearRepository financialYearRepository;

    public AccountingBootstrapService(AccountRepository accountRepository,
                                      FinancialYearRepository financialYearRepository) {
        this.accountRepository = accountRepository;
        this.financialYearRepository = financialYearRepository;
    }

    @Transactional
    public void bootstrap(UUID instituteId) {
        if (!accountRepository.existsByInstituteId(instituteId)) {
            for (Seed s : DEFAULT_ACCOUNTS) {
                Account a = new Account();
                a.setInstituteId(instituteId);
                a.setCode(s.code());
                a.setName(s.name());
                a.setType(s.type());
                a.setSystemKey(s.key());
                a.setActive(true);
                accountRepository.save(a);
            }
        }
        if (!financialYearRepository.existsByInstituteId(instituteId)) {
            int y = Year.now().getValue();
            FinancialYear fy = new FinancialYear();
            fy.setInstituteId(instituteId);
            fy.setName("FY " + y);
            fy.setStartDate(LocalDate.of(y, 1, 1));
            fy.setEndDate(LocalDate.of(y, 12, 31));
            fy.setCurrent(true);
            fy.setClosed(false);
            financialYearRepository.save(fy);
        }
    }
}
