package com.ims.tenant;

import java.util.UUID;

/**
 * Holds the current request's tenant (institute) id in a ThreadLocal.
 * Populated by {@link com.ims.auth.JwtAuthFilter} from the JWT claim.
 * A null tenant with superAdmin=true means the SaaS operator (bypasses the tenant filter).
 */
public final class TenantContext {

    private static final ThreadLocal<UUID> TENANT = new ThreadLocal<>();
    private static final ThreadLocal<Boolean> SUPER_ADMIN = ThreadLocal.withInitial(() -> Boolean.FALSE);

    private TenantContext() {
    }

    public static void set(UUID instituteId, boolean superAdmin) {
        TENANT.set(instituteId);
        SUPER_ADMIN.set(superAdmin);
    }

    public static UUID getTenantId() {
        return TENANT.get();
    }

    public static boolean isSuperAdmin() {
        return Boolean.TRUE.equals(SUPER_ADMIN.get());
    }

    public static void clear() {
        TENANT.remove();
        SUPER_ADMIN.remove();
    }
}
