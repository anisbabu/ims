package com.ims.staff;

import com.ims.common.NotFoundException;
import com.ims.common.PageResponse;
import com.ims.staff.dto.EmployeeDtos.CreateEmployee;
import com.ims.staff.dto.EmployeeDtos.EmployeeResponse;
import com.ims.staff.dto.EmployeeDtos.UpdateEmployee;
import com.ims.tenant.TenantGuard;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.UUID;

@Service
public class EmployeeService {

    private final EmployeeRepository repository;

    public EmployeeService(EmployeeRepository repository) {
        this.repository = repository;
    }

    @Transactional
    public EmployeeResponse create(CreateEmployee req) {
        Employee e = new Employee();
        e.setFullName(req.fullName());
        e.setDesignation(req.designation());
        e.setDob(req.dob());
        e.setGender(req.gender());
        e.setPhone(req.phone());
        e.setEmail(req.email());
        e.setAddress(req.address());
        e.setJoinDate(req.joinDate());
        e.setPhotoUrl(req.photoUrl());
        return EmployeeResponse.from(repository.save(e));
    }

    @Transactional(readOnly = true)
    public PageResponse<EmployeeResponse> list(String q, Pageable pageable) {
        Page<Employee> page = StringUtils.hasText(q)
                ? repository.findByFullNameContainingIgnoreCase(q, pageable)
                : repository.findAll(pageable);
        return PageResponse.from(page, EmployeeResponse::from);
    }

    @Transactional(readOnly = true)
    public EmployeeResponse get(UUID id) {
        return EmployeeResponse.from(require(id));
    }

    @Transactional
    public EmployeeResponse update(UUID id, UpdateEmployee req) {
        Employee e = require(id);
        if (req.fullName() != null) e.setFullName(req.fullName());
        if (req.designation() != null) e.setDesignation(req.designation());
        if (req.dob() != null) e.setDob(req.dob());
        if (req.gender() != null) e.setGender(req.gender());
        if (req.phone() != null) e.setPhone(req.phone());
        if (req.email() != null) e.setEmail(req.email());
        if (req.address() != null) e.setAddress(req.address());
        if (req.joinDate() != null) e.setJoinDate(req.joinDate());
        if (req.status() != null) e.setStatus(req.status());
        if (req.photoUrl() != null) e.setPhotoUrl(req.photoUrl());
        return EmployeeResponse.from(e);
    }

    @Transactional
    public void delete(UUID id) {
        repository.delete(require(id));
    }

    private Employee require(UUID id) {
        return repository.findById(id).map(TenantGuard::owned)
                .orElseThrow(() -> new NotFoundException("Employee not found"));
    }
}
