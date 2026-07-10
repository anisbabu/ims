package com.ims.config;

import com.ims.auth.SecurityUser;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.data.domain.AuditorAware;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import java.util.Optional;
import java.util.UUID;

/**
 * Pins the @Transactional advisor to the highest precedence so it wraps
 * {@link com.ims.tenant.TenantFilterAspect} — the tenant filter is enabled on a
 * Session that already exists inside an open transaction.
 */
@Configuration
@EnableTransactionManagement(order = Ordered.HIGHEST_PRECEDENCE)
public class PersistenceConfig {

    @Bean
    public AuditorAware<UUID> auditorAware() {
        return () -> {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            if (auth == null || !auth.isAuthenticated()) {
                return Optional.empty();
            }
            Object principal = auth.getPrincipal();
            if (principal instanceof SecurityUser su) {
                return Optional.ofNullable(su.getId());
            }
            return Optional.empty();
        };
    }
}
