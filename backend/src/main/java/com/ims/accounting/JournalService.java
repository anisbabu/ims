package com.ims.accounting;

import com.ims.accounting.dto.AccountingDtos.CreateJournal;
import com.ims.accounting.dto.AccountingDtos.JournalLineInput;
import com.ims.accounting.dto.AccountingDtos.JournalLineResponse;
import com.ims.accounting.dto.AccountingDtos.JournalResponse;
import com.ims.common.BadRequestException;
import com.ims.common.NotFoundException;
import com.ims.common.PageResponse;
import com.ims.tenant.TenantGuard;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class JournalService {

    private final JournalEntryRepository entryRepository;
    private final JournalLineRepository lineRepository;
    private final AccountRepository accountRepository;
    private final FinancialYearRepository financialYearRepository;

    public JournalService(JournalEntryRepository entryRepository,
                          JournalLineRepository lineRepository,
                          AccountRepository accountRepository,
                          FinancialYearRepository financialYearRepository) {
        this.entryRepository = entryRepository;
        this.lineRepository = lineRepository;
        this.accountRepository = accountRepository;
        this.financialYearRepository = financialYearRepository;
    }

    @Transactional
    public JournalResponse create(CreateJournal req) {
        FinancialYear fy = financialYearRepository.findById(req.financialYearId()).map(TenantGuard::owned)
                .orElseThrow(() -> new BadRequestException("Financial year not found"));
        if (req.post() && fy.isClosed()) {
            throw new BadRequestException("Financial year is closed");
        }
        validateBalanced(req.lines());

        JournalEntry entry = new JournalEntry();
        entry.setFinancialYearId(fy.getId());
        entry.setEntryDate(req.entryDate());
        entry.setReference(req.reference());
        entry.setNarration(req.narration());
        entry.setSource(JournalSource.MANUAL);
        entry.setPosted(req.post());
        JournalEntry saved = entryRepository.save(entry);

        for (JournalLineInput in : req.lines()) {
            persistLine(saved.getId(), in.accountId(), nz(in.debit()), nz(in.credit()), in.memo());
        }
        return toResponse(saved);
    }

    /** Creates a posted, system-sourced entry. Used by automatic postings. */
    @Transactional
    public JournalEntry createAuto(UUID financialYearId, LocalDate date, String reference,
                                   String narration, String sourceType, UUID sourceId,
                                   List<JournalLineInput> lines) {
        validateBalanced(lines);
        JournalEntry entry = new JournalEntry();
        entry.setFinancialYearId(financialYearId);
        entry.setEntryDate(date);
        entry.setReference(reference);
        entry.setNarration(narration);
        entry.setSource(JournalSource.AUTO);
        entry.setSourceType(sourceType);
        entry.setSourceId(sourceId);
        entry.setPosted(true);
        JournalEntry saved = entryRepository.save(entry);
        for (JournalLineInput in : lines) {
            persistLine(saved.getId(), in.accountId(), nz(in.debit()), nz(in.credit()), in.memo());
        }
        return saved;
    }

    @Transactional(readOnly = true)
    public PageResponse<JournalResponse> list(UUID financialYearId, Pageable pageable) {
        var page = financialYearId != null
                ? entryRepository.findByFinancialYearId(financialYearId, pageable)
                : entryRepository.findAll(pageable);
        return PageResponse.from(page, this::toResponse);
    }

    @Transactional(readOnly = true)
    public JournalResponse get(UUID id) {
        JournalEntry entry = entryRepository.findById(id).map(TenantGuard::owned)
                .orElseThrow(() -> new NotFoundException("Journal not found"));
        return toResponse(entry);
    }

    @Transactional
    public JournalResponse post(UUID id) {
        JournalEntry entry = entryRepository.findById(id).map(TenantGuard::owned)
                .orElseThrow(() -> new NotFoundException("Journal not found"));
        FinancialYear fy = financialYearRepository.findById(entry.getFinancialYearId())
                .orElseThrow(() -> new BadRequestException("Financial year not found"));
        if (fy.isClosed()) {
            throw new BadRequestException("Financial year is closed");
        }
        entry.setPosted(true);
        return toResponse(entry);
    }

    // ---- helpers ----

    private void validateBalanced(List<JournalLineInput> lines) {
        if (lines == null || lines.size() < 2) {
            throw new BadRequestException("A journal needs at least two lines");
        }
        BigDecimal totalDebit = BigDecimal.ZERO;
        BigDecimal totalCredit = BigDecimal.ZERO;
        for (JournalLineInput in : lines) {
            BigDecimal d = nz(in.debit());
            BigDecimal c = nz(in.credit());
            if (d.signum() < 0 || c.signum() < 0) {
                throw new BadRequestException("Debit/credit cannot be negative");
            }
            if ((d.signum() > 0) == (c.signum() > 0)) {
                throw new BadRequestException("Each line must have exactly one of debit or credit");
            }
            accountRepository.findById(in.accountId()).map(TenantGuard::owned)
                    .filter(Account::isActive)
                    .orElseThrow(() -> new BadRequestException("Account not found or inactive: " + in.accountId()));
            totalDebit = totalDebit.add(d);
            totalCredit = totalCredit.add(c);
        }
        if (totalDebit.compareTo(totalCredit) != 0) {
            throw new BadRequestException("Journal not balanced: debit " + totalDebit + " != credit " + totalCredit);
        }
        if (totalDebit.signum() == 0) {
            throw new BadRequestException("Journal total cannot be zero");
        }
    }

    private void persistLine(UUID entryId, UUID accountId, BigDecimal debit, BigDecimal credit, String memo) {
        JournalLine line = new JournalLine();
        line.setJournalEntryId(entryId);
        line.setAccountId(accountId);
        line.setDebit(debit);
        line.setCredit(credit);
        line.setMemo(memo);
        lineRepository.save(line);
    }

    private JournalResponse toResponse(JournalEntry entry) {
        List<JournalLine> lines = lineRepository.findByJournalEntryId(entry.getId());
        Map<UUID, Account> accounts = accountRepository.findAll().stream()
                .collect(Collectors.toMap(Account::getId, Function.identity()));
        BigDecimal totalDebit = BigDecimal.ZERO;
        BigDecimal totalCredit = BigDecimal.ZERO;
        List<JournalLineResponse> lineResponses = new ArrayList<>();
        for (JournalLine l : lines) {
            Account a = accounts.get(l.getAccountId());
            lineResponses.add(new JournalLineResponse(l.getId(), l.getAccountId(),
                    a != null ? a.getCode() : "?", a != null ? a.getName() : "?",
                    l.getDebit(), l.getCredit(), l.getMemo()));
            totalDebit = totalDebit.add(l.getDebit());
            totalCredit = totalCredit.add(l.getCredit());
        }
        return new JournalResponse(entry.getId(), entry.getFinancialYearId(), entry.getEntryDate(),
                entry.getReference(), entry.getNarration(), entry.getSource(), entry.getSourceType(),
                entry.getSourceId(), entry.isPosted(), totalDebit, totalCredit, lineResponses);
    }

    private static BigDecimal nz(BigDecimal v) {
        return v == null ? BigDecimal.ZERO : v;
    }
}
