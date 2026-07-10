package com.ims.auth;

import com.ims.auth.dto.UserDtos.CreateUser;
import com.ims.auth.dto.UserDtos.UpdateUser;
import com.ims.auth.dto.UserDtos.UserResponse;
import com.ims.common.BadRequestException;
import com.ims.common.NotFoundException;
import com.ims.common.PageResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

/**
 * Tenant-aware user administration. Users are not behind the Hibernate tenant filter
 * (login resolves them globally by email), so every method authorizes the actor against
 * the target's institute explicitly.
 *
 * <ul>
 *   <li>SUPER_ADMIN: manage any user in any institute; may create SUPER_ADMINs.</li>
 *   <li>INSTITUTE_ADMIN: manage users only within their own institute; may not create or
 *       grant the SUPER_ADMIN role.</li>
 * </ul>
 */
@Service
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional
    public UserResponse create(SecurityUser actor, CreateUser req) {
        if (userRepository.existsByEmailIgnoreCase(req.email())) {
            throw new BadRequestException("Email already in use: " + req.email());
        }
        UUID targetInstitute = resolveTargetInstitute(actor, req.instituteId(), req.role());
        assertCanAssignRole(actor, req.role());

        User u = new User();
        u.setEmail(req.email());
        u.setPasswordHash(passwordEncoder.encode(req.password()));
        u.setFullName(req.fullName());
        u.setRole(req.role());
        u.setStatus(UserStatus.ACTIVE);
        u.setInstituteId(targetInstitute);
        u.setProfileId(req.profileId());
        return UserResponse.from(userRepository.save(u));
    }

    @Transactional(readOnly = true)
    public PageResponse<UserResponse> list(SecurityUser actor, Role role, Pageable pageable) {
        Page<User> page;
        if (actor.getRole() == Role.SUPER_ADMIN) {
            page = role != null ? userRepository.findByRole(role, pageable) : userRepository.findAll(pageable);
        } else {
            UUID inst = actor.getInstituteId();
            page = role != null
                    ? userRepository.findByInstituteIdAndRole(inst, role, pageable)
                    : userRepository.findByInstituteId(inst, pageable);
        }
        return PageResponse.from(page, UserResponse::from);
    }

    @Transactional(readOnly = true)
    public UserResponse get(SecurityUser actor, UUID id) {
        return UserResponse.from(requireVisible(actor, id));
    }

    @Transactional
    public UserResponse update(SecurityUser actor, UUID id, UpdateUser req) {
        User u = requireVisible(actor, id);
        if (req.fullName() != null) u.setFullName(req.fullName());
        if (req.profileId() != null) u.setProfileId(req.profileId());
        if (req.status() != null) {
            if (u.getId().equals(actor.getId()) && req.status() == UserStatus.DISABLED) {
                throw new BadRequestException("You cannot disable your own account");
            }
            u.setStatus(req.status());
        }
        if (req.role() != null && req.role() != u.getRole()) {
            assertCanAssignRole(actor, req.role());
            if (u.getId().equals(actor.getId())) {
                throw new BadRequestException("You cannot change your own role");
            }
            u.setRole(req.role());
        }
        return UserResponse.from(u);
    }

    @Transactional
    public void resetPassword(SecurityUser actor, UUID id, String newPassword) {
        User u = requireVisible(actor, id);
        u.setPasswordHash(passwordEncoder.encode(newPassword));
    }

    @Transactional
    public void delete(SecurityUser actor, UUID id) {
        User u = requireVisible(actor, id);
        if (u.getId().equals(actor.getId())) {
            throw new BadRequestException("You cannot delete your own account");
        }
        userRepository.delete(u);
    }

    @Transactional
    public void changeOwnPassword(UUID userId, String current, String next) {
        User u = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("User not found"));
        if (!passwordEncoder.matches(current, u.getPasswordHash())) {
            throw new BadRequestException("Current password is incorrect");
        }
        u.setPasswordHash(passwordEncoder.encode(next));
    }

    // ---- authorization helpers ----

    private User requireVisible(SecurityUser actor, UUID id) {
        User u = userRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("User not found"));
        if (actor.getRole() != Role.SUPER_ADMIN
                && (u.getInstituteId() == null || !u.getInstituteId().equals(actor.getInstituteId()))) {
            throw new NotFoundException("User not found");
        }
        return u;
    }

    private UUID resolveTargetInstitute(SecurityUser actor, UUID requested, Role role) {
        if (actor.getRole() == Role.SUPER_ADMIN) {
            if (role == Role.SUPER_ADMIN) {
                return null; // platform-level user, not tied to a tenant
            }
            if (requested == null) {
                throw new BadRequestException("instituteId is required for a tenant user");
            }
            return requested;
        }
        // Institute admin: always their own tenant.
        if (actor.getInstituteId() == null) {
            throw new AccessDeniedException("No institute bound to this user");
        }
        return actor.getInstituteId();
    }

    private void assertCanAssignRole(SecurityUser actor, Role role) {
        if (role == Role.SUPER_ADMIN && actor.getRole() != Role.SUPER_ADMIN) {
            throw new AccessDeniedException("Only a super admin can assign the SUPER_ADMIN role");
        }
    }
}
