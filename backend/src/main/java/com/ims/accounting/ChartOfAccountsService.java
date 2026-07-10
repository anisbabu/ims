package com.ims.accounting;

import com.ims.accounting.dto.AccountingDtos.AccountResponse;
import com.ims.accounting.dto.AccountingDtos.CreateAccount;
import com.ims.common.BadRequestException;
import com.ims.common.NotFoundException;
import com.ims.tenant.TenantGuard;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.List;
import java.util.UUID;

@Service
public class ChartOfAccountsService {

    private final AccountRepository accountRepository;

    public ChartOfAccountsService(AccountRepository accountRepository) {
        this.accountRepository = accountRepository;
    }

    @Transactional
    public AccountResponse create(CreateAccount req) {
        boolean exists = accountRepository.findAll().stream()
                .anyMatch(a -> a.getCode().equalsIgnoreCase(req.code()));
        if (exists) {
            throw new BadRequestException("Account code already exists: " + req.code());
        }
        if (req.parentId() != null) {
            accountRepository.findById(req.parentId()).map(TenantGuard::owned)
                    .orElseThrow(() -> new BadRequestException("Parent account not found"));
        }
        Account a = new Account();
        a.setCode(req.code());
        a.setName(req.name());
        a.setType(req.type());
        a.setParentId(req.parentId());
        a.setActive(true);
        return AccountResponse.from(accountRepository.save(a));
    }

    @Transactional(readOnly = true)
    public List<AccountResponse> list() {
        return accountRepository.findAll().stream()
                .sorted(Comparator.comparing(Account::getCode))
                .map(AccountResponse::from).toList();
    }

    @Transactional(readOnly = true)
    public AccountResponse get(UUID id) {
        return AccountResponse.from(accountRepository.findById(id).map(TenantGuard::owned)
                .orElseThrow(() -> new NotFoundException("Account not found")));
    }
}
