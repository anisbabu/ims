package com.ims.institute;

import com.ims.accounting.AccountingBootstrapService;
import com.ims.auth.Role;
import com.ims.auth.User;
import com.ims.auth.UserRepository;
import com.ims.auth.UserStatus;
import com.ims.common.BadRequestException;
import com.ims.common.NotFoundException;
import com.ims.institute.dto.CreateInstituteRequest;
import com.ims.institute.dto.InstituteResponse;
import com.ims.institute.dto.UpdateInstituteRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.ims.common.PageResponse;

import java.util.UUID;

@Service
public class InstituteService {

    private final InstituteRepository instituteRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AccountingBootstrapService accountingBootstrapService;

    public InstituteService(InstituteRepository instituteRepository,
                            UserRepository userRepository,
                            PasswordEncoder passwordEncoder,
                            AccountingBootstrapService accountingBootstrapService) {
        this.instituteRepository = instituteRepository;
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.accountingBootstrapService = accountingBootstrapService;
    }

    /** Super-admin: create an institute together with its first INSTITUTE_ADMIN user. */
    @Transactional
    public InstituteResponse create(CreateInstituteRequest req) {
        if (instituteRepository.existsByCodeIgnoreCase(req.code())) {
            throw new BadRequestException("Institute code already exists: " + req.code());
        }
        if (userRepository.existsByEmailIgnoreCase(req.adminEmail())) {
            throw new BadRequestException("Admin email already in use: " + req.adminEmail());
        }

        Institute institute = new Institute();
        institute.setName(req.name());
        institute.setCode(req.code());
        institute.setAddress(req.address());
        institute.setPhone(req.phone());
        institute.setEmail(req.email());
        institute = instituteRepository.save(institute);

        User admin = new User();
        admin.setEmail(req.adminEmail());
        admin.setPasswordHash(passwordEncoder.encode(req.adminPassword()));
        admin.setFullName(req.adminFullName());
        admin.setRole(Role.INSTITUTE_ADMIN);
        admin.setStatus(UserStatus.ACTIVE);
        admin.setInstituteId(institute.getId());
        userRepository.save(admin);

        accountingBootstrapService.bootstrap(institute.getId());

        return InstituteResponse.from(institute);
    }

    @Transactional(readOnly = true)
    public PageResponse<InstituteResponse> list(Pageable pageable) {
        return PageResponse.from(instituteRepository.findAll(pageable), InstituteResponse::from);
    }

    @Transactional(readOnly = true)
    public InstituteResponse get(UUID id) {
        Institute institute = instituteRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Institute not found"));
        return InstituteResponse.from(institute);
    }

    @Transactional
    public InstituteResponse update(UUID id, UpdateInstituteRequest req) {
        Institute institute = instituteRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Institute not found"));
        if (req.name() != null) institute.setName(req.name());
        if (req.address() != null) institute.setAddress(req.address());
        if (req.phone() != null) institute.setPhone(req.phone());
        if (req.email() != null) institute.setEmail(req.email());
        if (req.logoUrl() != null) institute.setLogoUrl(req.logoUrl());
        if (req.settings() != null) institute.setSettings(req.settings());
        return InstituteResponse.from(institute);
    }
}
