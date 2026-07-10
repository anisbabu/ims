package com.ims.accounting;

import com.ims.accounting.dto.AccountingDtos.BalanceSheet;
import com.ims.accounting.dto.AccountingDtos.Ledger;
import com.ims.accounting.dto.AccountingDtos.ProfitAndLoss;
import com.ims.accounting.dto.AccountingDtos.TrialBalance;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/api/accounting/reports")
@PreAuthorize("hasAnyRole('SUPER_ADMIN','INSTITUTE_ADMIN')")
public class ReportController {

    private final ReportService reportService;

    public ReportController(ReportService reportService) {
        this.reportService = reportService;
    }

    @GetMapping("/trial-balance")
    public TrialBalance trialBalance(@RequestParam UUID financialYearId) {
        return reportService.trialBalance(financialYearId);
    }

    @GetMapping("/profit-and-loss")
    public ProfitAndLoss profitAndLoss(@RequestParam UUID financialYearId) {
        return reportService.profitAndLoss(financialYearId);
    }

    @GetMapping("/balance-sheet")
    public BalanceSheet balanceSheet(@RequestParam UUID financialYearId) {
        return reportService.balanceSheet(financialYearId);
    }

    @GetMapping("/ledger")
    public Ledger ledger(@RequestParam UUID financialYearId, @RequestParam UUID accountId) {
        return reportService.ledger(financialYearId, accountId);
    }
}
