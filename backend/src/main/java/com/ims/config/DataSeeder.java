package com.ims.config;

import com.ims.accounting.AccountingBootstrapService;
import com.ims.auth.Role;
import com.ims.auth.User;
import com.ims.auth.UserRepository;
import com.ims.auth.UserStatus;
import com.ims.institute.Institute;
import com.ims.institute.InstituteRepository;
import com.ims.institute.InstituteStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

/**
 * Idempotent dev seed: one platform SUPER_ADMIN, one demo institute plus its
 * INSTITUTE_ADMIN. Disable with ims.seed.enabled=false. Not for production.
 */
@Component
@ConditionalOnProperty(name = "ims.seed.enabled", havingValue = "true", matchIfMissing = true)
public class DataSeeder implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(DataSeeder.class);

    private static final String SUPER_ADMIN_EMAIL = "super@ims.local";
    private static final String DEMO_CODE = "DEMO";
    private static final String DEMO_ADMIN_EMAIL = "admin@demo.local";
    private static final String DEFAULT_PASSWORD = "Admin12345";

    private final UserRepository userRepository;
    private final InstituteRepository instituteRepository;
    private final PasswordEncoder passwordEncoder;
    private final AccountingBootstrapService accountingBootstrapService;

    public DataSeeder(UserRepository userRepository,
                      InstituteRepository instituteRepository,
                      PasswordEncoder passwordEncoder,
                      AccountingBootstrapService accountingBootstrapService) {
        this.userRepository = userRepository;
        this.instituteRepository = instituteRepository;
        this.passwordEncoder = passwordEncoder;
        this.accountingBootstrapService = accountingBootstrapService;
    }

    @Override
    public void run(String... args) {
        if (!userRepository.existsByEmailIgnoreCase(SUPER_ADMIN_EMAIL)) {
            User su = new User();
            su.setEmail(SUPER_ADMIN_EMAIL);
            su.setPasswordHash(passwordEncoder.encode(DEFAULT_PASSWORD));
            su.setFullName("Platform Super Admin");
            su.setRole(Role.SUPER_ADMIN);
            su.setStatus(UserStatus.ACTIVE);
            userRepository.save(su);
            log.info("Seeded SUPER_ADMIN: {} / {}", SUPER_ADMIN_EMAIL, DEFAULT_PASSWORD);
        }

        Institute demo = instituteRepository.findByCodeIgnoreCase(DEMO_CODE).orElse(null);
        if (demo == null) {
            demo = new Institute();
            demo.setName("Demo School");
            demo.setCode(DEMO_CODE);
            demo.setStatus(InstituteStatus.ACTIVE);
            demo = instituteRepository.save(demo);
            log.info("Seeded demo institute: {}", DEMO_CODE);
        }
        accountingBootstrapService.bootstrap(demo.getId());

        if (!userRepository.existsByEmailIgnoreCase(DEMO_ADMIN_EMAIL)) {
            User admin = new User();
            admin.setEmail(DEMO_ADMIN_EMAIL);
            admin.setPasswordHash(passwordEncoder.encode(DEFAULT_PASSWORD));
            admin.setFullName("Demo Institute Admin");
            admin.setRole(Role.INSTITUTE_ADMIN);
            admin.setStatus(UserStatus.ACTIVE);
            admin.setInstituteId(demo.getId());
            userRepository.save(admin);
            log.info("Seeded INSTITUTE_ADMIN: {} / {}", DEMO_ADMIN_EMAIL, DEFAULT_PASSWORD);
        }
    }
}
