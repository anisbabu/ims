package com.ims.accounting;

import com.ims.accounting.dto.AccountingDtos.BalanceSheet;
import com.ims.accounting.dto.AccountingDtos.Ledger;
import com.ims.accounting.dto.AccountingDtos.LedgerRow;
import com.ims.accounting.dto.AccountingDtos.ProfitAndLoss;
import com.ims.accounting.dto.AccountingDtos.ReportLine;
import com.ims.accounting.dto.AccountingDtos.TrialBalance;
import com.ims.accounting.dto.AccountingDtos.TrialBalanceRow;
import com.ims.common.BadRequestException;
import com.ims.common.NotFoundException;
import com.ims.tenant.TenantGuard;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class ReportService {

    private final JournalLineRepository lineRepository;
    private final JournalEntryRepository entryRepository;
    private final AccountRepository accountRepository;
    private final FinancialYearRepository financialYearRepository;

    public ReportService(JournalLineRepository lineRepository,
                         JournalEntryRepository entryRepository,
                         AccountRepository accountRepository,
                         FinancialYearRepository financialYearRepository) {
        this.lineRepository = lineRepository;
        this.entryRepository = entryRepository;
        this.accountRepository = accountRepository;
        this.financialYearRepository = financialYearRepository;
    }

    private record Totals(BigDecimal debit, BigDecimal credit) {
        Totals add(BigDecimal d, BigDecimal c) {
            return new Totals(debit.add(d), credit.add(c));
        }

        static Totals zero() {
            return new Totals(BigDecimal.ZERO, BigDecimal.ZERO);
        }
    }

    private Map<UUID, Account> accountMap() {
        return accountRepository.findAll().stream()
                .collect(Collectors.toMap(Account::getId, Function.identity()));
    }

    private Map<UUID, Totals> totalsByAccount(UUID fyId) {
        Map<UUID, Totals> map = new LinkedHashMap<>();
        for (JournalLine l : lineRepository.findPostedByFinancialYear(fyId)) {
            map.merge(l.getAccountId(), Totals.zero().add(l.getDebit(), l.getCredit()),
                    (a, b) -> a.add(b.debit(), b.credit()));
        }
        return map;
    }

    @Transactional(readOnly = true)
    public TrialBalance trialBalance(UUID fyId) {
        requireYear(fyId);
        Map<UUID, Account> accounts = accountMap();
        Map<UUID, Totals> totals = totalsByAccount(fyId);
        List<TrialBalanceRow> rows = new ArrayList<>();
        BigDecimal totalDebit = BigDecimal.ZERO;
        BigDecimal totalCredit = BigDecimal.ZERO;
        for (var e : totals.entrySet()) {
            Account a = accounts.get(e.getKey());
            if (a == null) continue;
            BigDecimal net = e.getValue().debit().subtract(e.getValue().credit());
            BigDecimal debitCol = net.signum() > 0 ? net : BigDecimal.ZERO;
            BigDecimal creditCol = net.signum() < 0 ? net.negate() : BigDecimal.ZERO;
            if (debitCol.signum() == 0 && creditCol.signum() == 0) continue;
            rows.add(new TrialBalanceRow(a.getId(), a.getCode(), a.getName(), a.getType(), debitCol, creditCol));
            totalDebit = totalDebit.add(debitCol);
            totalCredit = totalCredit.add(creditCol);
        }
        rows.sort((x, y) -> x.code().compareTo(y.code()));
        return new TrialBalance(fyId, rows, totalDebit, totalCredit,
                totalDebit.compareTo(totalCredit) == 0);
    }

    @Transactional(readOnly = true)
    public ProfitAndLoss profitAndLoss(UUID fyId) {
        requireYear(fyId);
        Map<UUID, Account> accounts = accountMap();
        Map<UUID, Totals> totals = totalsByAccount(fyId);
        List<ReportLine> income = new ArrayList<>();
        List<ReportLine> expense = new ArrayList<>();
        BigDecimal totalIncome = BigDecimal.ZERO;
        BigDecimal totalExpense = BigDecimal.ZERO;
        for (var e : totals.entrySet()) {
            Account a = accounts.get(e.getKey());
            if (a == null) continue;
            Totals t = e.getValue();
            if (a.getType() == AccountType.INCOME) {
                BigDecimal amt = t.credit().subtract(t.debit());
                income.add(new ReportLine(a.getId(), a.getCode(), a.getName(), amt));
                totalIncome = totalIncome.add(amt);
            } else if (a.getType() == AccountType.EXPENSE) {
                BigDecimal amt = t.debit().subtract(t.credit());
                expense.add(new ReportLine(a.getId(), a.getCode(), a.getName(), amt));
                totalExpense = totalExpense.add(amt);
            }
        }
        income.sort((x, y) -> x.code().compareTo(y.code()));
        expense.sort((x, y) -> x.code().compareTo(y.code()));
        return new ProfitAndLoss(fyId, income, totalIncome, expense, totalExpense,
                totalIncome.subtract(totalExpense));
    }

    @Transactional(readOnly = true)
    public BalanceSheet balanceSheet(UUID fyId) {
        FinancialYear fy = requireYear(fyId);
        Map<UUID, Account> accounts = accountMap();
        Map<UUID, Totals> totals = totalsByAccount(fyId);
        List<ReportLine> assets = new ArrayList<>();
        List<ReportLine> liabilities = new ArrayList<>();
        List<ReportLine> equity = new ArrayList<>();
        BigDecimal totalAssets = BigDecimal.ZERO;
        BigDecimal totalLiabilities = BigDecimal.ZERO;
        BigDecimal totalEquity = BigDecimal.ZERO;
        BigDecimal totalIncome = BigDecimal.ZERO;
        BigDecimal totalExpense = BigDecimal.ZERO;
        for (var e : totals.entrySet()) {
            Account a = accounts.get(e.getKey());
            if (a == null) continue;
            Totals t = e.getValue();
            switch (a.getType()) {
                case ASSET -> {
                    BigDecimal amt = t.debit().subtract(t.credit());
                    assets.add(new ReportLine(a.getId(), a.getCode(), a.getName(), amt));
                    totalAssets = totalAssets.add(amt);
                }
                case LIABILITY -> {
                    BigDecimal amt = t.credit().subtract(t.debit());
                    liabilities.add(new ReportLine(a.getId(), a.getCode(), a.getName(), amt));
                    totalLiabilities = totalLiabilities.add(amt);
                }
                case EQUITY -> {
                    BigDecimal amt = t.credit().subtract(t.debit());
                    equity.add(new ReportLine(a.getId(), a.getCode(), a.getName(), amt));
                    totalEquity = totalEquity.add(amt);
                }
                case INCOME -> totalIncome = totalIncome.add(t.credit().subtract(t.debit()));
                case EXPENSE -> totalExpense = totalExpense.add(t.debit().subtract(t.credit()));
            }
        }
        BigDecimal netProfit = totalIncome.subtract(totalExpense);
        BigDecimal liabPlusEquity = totalLiabilities.add(totalEquity).add(netProfit);
        assets.sort((x, y) -> x.code().compareTo(y.code()));
        liabilities.sort((x, y) -> x.code().compareTo(y.code()));
        equity.sort((x, y) -> x.code().compareTo(y.code()));
        return new BalanceSheet(fyId, fy.getEndDate(), assets, totalAssets,
                liabilities, totalLiabilities, equity, totalEquity, netProfit,
                liabPlusEquity, totalAssets.compareTo(liabPlusEquity) == 0);
    }

    @Transactional(readOnly = true)
    public Ledger ledger(UUID fyId, UUID accountId) {
        requireYear(fyId);
        Account account = accountRepository.findById(accountId).map(TenantGuard::owned)
                .orElseThrow(() -> new NotFoundException("Account not found"));
        boolean debitNormal = account.getType().isNormalDebit();
        List<JournalLine> lines = lineRepository.findPostedByFinancialYearAndAccount(fyId, accountId);
        Map<UUID, JournalEntry> entries = entryRepository.findAll().stream()
                .collect(Collectors.toMap(JournalEntry::getId, Function.identity()));
        BigDecimal running = BigDecimal.ZERO;
        List<LedgerRow> rows = new ArrayList<>();
        for (JournalLine l : lines) {
            BigDecimal delta = debitNormal
                    ? l.getDebit().subtract(l.getCredit())
                    : l.getCredit().subtract(l.getDebit());
            running = running.add(delta);
            JournalEntry en = entries.get(l.getJournalEntryId());
            rows.add(new LedgerRow(
                    en != null ? en.getEntryDate() : null,
                    l.getJournalEntryId(),
                    en != null ? en.getReference() : null,
                    en != null ? en.getNarration() : null,
                    l.getDebit(), l.getCredit(), running));
        }
        return new Ledger(account.getId(), account.getCode(), account.getName(), account.getType(),
                BigDecimal.ZERO, rows, running);
    }

    private FinancialYear requireYear(UUID fyId) {
        return financialYearRepository.findById(fyId).map(TenantGuard::owned)
                .orElseThrow(() -> new BadRequestException("Financial year not found"));
    }
}
