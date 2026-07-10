package com.ims.accounting;

import com.ims.accounting.dto.AccountingDtos.CreateFinancialYear;
import com.ims.accounting.dto.AccountingDtos.FinancialYearResponse;
import com.ims.common.BadRequestException;
import com.ims.common.NotFoundException;
import com.ims.tenant.TenantGuard;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.List;
import java.util.UUID;

@Service
public class FinancialYearService {

    private final FinancialYearRepository repository;

    public FinancialYearService(FinancialYearRepository repository) {
        this.repository = repository;
    }

    @Transactional
    public FinancialYearResponse create(CreateFinancialYear req) {
        if (!req.endDate().isAfter(req.startDate())) {
            throw new BadRequestException("End date must be after start date");
        }
        FinancialYear fy = new FinancialYear();
        fy.setName(req.name());
        fy.setStartDate(req.startDate());
        fy.setEndDate(req.endDate());
        fy.setClosed(false);
        if (req.current()) {
            repository.findByCurrentTrue().forEach(existing -> existing.setCurrent(false));
        }
        fy.setCurrent(req.current());
        return FinancialYearResponse.from(repository.save(fy));
    }

    @Transactional(readOnly = true)
    public List<FinancialYearResponse> list() {
        return repository.findAll().stream()
                .sorted(Comparator.comparing(FinancialYear::getStartDate).reversed())
                .map(FinancialYearResponse::from).toList();
    }

    @Transactional
    public FinancialYearResponse setCurrent(UUID id) {
        FinancialYear fy = require(id);
        repository.findByCurrentTrue().forEach(existing -> existing.setCurrent(false));
        fy.setCurrent(true);
        return FinancialYearResponse.from(fy);
    }

    @Transactional
    public FinancialYearResponse close(UUID id) {
        FinancialYear fy = require(id);
        fy.setClosed(true);
        return FinancialYearResponse.from(fy);
    }

    private FinancialYear require(UUID id) {
        return repository.findById(id).map(TenantGuard::owned)
                .orElseThrow(() -> new NotFoundException("Financial year not found"));
    }
}
