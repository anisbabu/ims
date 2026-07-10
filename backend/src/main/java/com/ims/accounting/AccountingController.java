package com.ims.accounting;

import com.ims.accounting.dto.AccountingDtos.AccountResponse;
import com.ims.accounting.dto.AccountingDtos.CreateAccount;
import com.ims.accounting.dto.AccountingDtos.CreateFinancialYear;
import com.ims.accounting.dto.AccountingDtos.FinancialYearResponse;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/accounting")
@PreAuthorize("hasAnyRole('SUPER_ADMIN','INSTITUTE_ADMIN')")
public class AccountingController {

    private final ChartOfAccountsService coaService;
    private final FinancialYearService fyService;

    public AccountingController(ChartOfAccountsService coaService, FinancialYearService fyService) {
        this.coaService = coaService;
        this.fyService = fyService;
    }

    // Chart of accounts
    @PostMapping("/accounts")
    @ResponseStatus(HttpStatus.CREATED)
    public AccountResponse createAccount(@Valid @RequestBody CreateAccount req) {
        return coaService.create(req);
    }

    @GetMapping("/accounts")
    public List<AccountResponse> listAccounts() {
        return coaService.list();
    }

    @GetMapping("/accounts/{id}")
    public AccountResponse getAccount(@PathVariable UUID id) {
        return coaService.get(id);
    }

    // Financial years
    @PostMapping("/financial-years")
    @ResponseStatus(HttpStatus.CREATED)
    public FinancialYearResponse createYear(@Valid @RequestBody CreateFinancialYear req) {
        return fyService.create(req);
    }

    @GetMapping("/financial-years")
    public List<FinancialYearResponse> listYears() {
        return fyService.list();
    }

    @PatchMapping("/financial-years/{id}/current")
    public FinancialYearResponse setCurrent(@PathVariable UUID id) {
        return fyService.setCurrent(id);
    }

    @PatchMapping("/financial-years/{id}/close")
    public FinancialYearResponse close(@PathVariable UUID id) {
        return fyService.close(id);
    }
}
