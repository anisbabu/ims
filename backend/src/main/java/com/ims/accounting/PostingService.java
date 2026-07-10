package com.ims.accounting;

import com.ims.accounting.dto.AccountingDtos.JournalLineInput;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Set;
import java.util.UUID;

/**
 * Generates automatic journal entries from business events (cash-basis).
 * Fee payment: Dr Cash/Bank, Cr Fee Income.
 */
@Service
public class PostingService {

    private static final Logger log = LoggerFactory.getLogger(PostingService.class);
    private static final Set<String> BANK_METHODS = Set.of("BANK", "CARD", "ONLINE");

    private final AccountRepository accountRepository;
    private final FinancialYearRepository financialYearRepository;
    private final JournalService journalService;

    public PostingService(AccountRepository accountRepository,
                          FinancialYearRepository financialYearRepository,
                          JournalService journalService) {
        this.accountRepository = accountRepository;
        this.financialYearRepository = financialYearRepository;
        this.journalService = journalService;
    }

    @Transactional
    public void postFeePayment(UUID paymentId, UUID studentId, BigDecimal amount,
                               String method, LocalDate date) {
        FinancialYear fy = financialYearRepository.findByCurrentTrue().stream().findFirst()
                .orElse(null);
        if (fy == null) {
            log.warn("No current financial year; skipping auto-journal for payment {}", paymentId);
            return;
        }
        String cashKey = BANK_METHODS.contains(method == null ? "" : method.toUpperCase()) ? "BANK" : "CASH";
        Account debitAccount = accountRepository.findBySystemKey(cashKey).orElse(null);
        Account incomeAccount = accountRepository.findBySystemKey("FEE_INCOME").orElse(null);
        if (debitAccount == null || incomeAccount == null) {
            log.warn("Missing system accounts ({}, FEE_INCOME); skipping auto-journal for payment {}",
                    cashKey, paymentId);
            return;
        }
        List<JournalLineInput> lines = List.of(
                new JournalLineInput(debitAccount.getId(), amount, BigDecimal.ZERO, "Fee payment received"),
                new JournalLineInput(incomeAccount.getId(), BigDecimal.ZERO, amount, "Fee income")
        );
        journalService.createAuto(fy.getId(), date, "PAY-" + paymentId.toString().substring(0, 8),
                "Auto: fee payment", "FEE_PAYMENT", paymentId, lines);
    }
}
