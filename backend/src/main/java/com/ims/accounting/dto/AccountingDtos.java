package com.ims.accounting.dto;

import com.ims.accounting.Account;
import com.ims.accounting.AccountType;
import com.ims.accounting.FinancialYear;
import com.ims.accounting.JournalEntry;
import com.ims.accounting.JournalLine;
import com.ims.accounting.JournalSource;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public final class AccountingDtos {

    private AccountingDtos() {
    }

    // ---- Financial year ----
    public record CreateFinancialYear(
            @NotBlank String name,
            @NotNull LocalDate startDate,
            @NotNull LocalDate endDate,
            boolean current) {
    }

    public record FinancialYearResponse(
            UUID id, String name, LocalDate startDate, LocalDate endDate,
            boolean current, boolean closed) {
        public static FinancialYearResponse from(FinancialYear f) {
            return new FinancialYearResponse(f.getId(), f.getName(), f.getStartDate(),
                    f.getEndDate(), f.isCurrent(), f.isClosed());
        }
    }

    // ---- Account ----
    public record CreateAccount(
            @NotBlank @Size(max = 32) String code,
            @NotBlank String name,
            @NotNull AccountType type,
            UUID parentId) {
    }

    public record AccountResponse(
            UUID id, String code, String name, AccountType type,
            UUID parentId, String systemKey, boolean active) {
        public static AccountResponse from(Account a) {
            return new AccountResponse(a.getId(), a.getCode(), a.getName(), a.getType(),
                    a.getParentId(), a.getSystemKey(), a.isActive());
        }
    }

    // ---- Journal ----
    public record JournalLineInput(
            @NotNull UUID accountId,
            BigDecimal debit,
            BigDecimal credit,
            String memo) {
    }

    public record CreateJournal(
            @NotNull UUID financialYearId,
            @NotNull LocalDate entryDate,
            String reference,
            String narration,
            @NotNull @Size(min = 2) List<JournalLineInput> lines,
            boolean post) {
    }

    public record JournalLineResponse(
            UUID id, UUID accountId, String accountCode, String accountName,
            BigDecimal debit, BigDecimal credit, String memo) {
    }

    public record JournalResponse(
            UUID id, UUID financialYearId, LocalDate entryDate, String reference,
            String narration, JournalSource source, String sourceType, UUID sourceId, boolean posted,
            BigDecimal totalDebit, BigDecimal totalCredit, List<JournalLineResponse> lines) {
    }

    // ---- Reports ----
    public record LedgerRow(
            LocalDate date, UUID journalEntryId, String reference, String narration,
            BigDecimal debit, BigDecimal credit, BigDecimal balance) {
    }

    public record Ledger(
            UUID accountId, String accountCode, String accountName, AccountType type,
            BigDecimal openingBalance, List<LedgerRow> rows, BigDecimal closingBalance) {
    }

    public record TrialBalanceRow(
            UUID accountId, String code, String name, AccountType type,
            BigDecimal debit, BigDecimal credit) {
    }

    public record TrialBalance(
            UUID financialYearId, List<TrialBalanceRow> rows,
            BigDecimal totalDebit, BigDecimal totalCredit, boolean balanced) {
    }

    public record ReportLine(UUID accountId, String code, String name, BigDecimal amount) {
    }

    public record ProfitAndLoss(
            UUID financialYearId,
            List<ReportLine> income, BigDecimal totalIncome,
            List<ReportLine> expense, BigDecimal totalExpense,
            BigDecimal netProfit) {
    }

    public record BalanceSheet(
            UUID financialYearId, LocalDate asOf,
            List<ReportLine> assets, BigDecimal totalAssets,
            List<ReportLine> liabilities, BigDecimal totalLiabilities,
            List<ReportLine> equity, BigDecimal totalEquity,
            BigDecimal netProfit, BigDecimal totalLiabilitiesAndEquity, boolean balanced) {
    }
}
