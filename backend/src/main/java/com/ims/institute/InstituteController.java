package com.ims.institute;

import com.ims.auth.Role;
import com.ims.auth.SecurityUser;
import com.ims.common.PageResponse;
import com.ims.institute.dto.CreateInstituteRequest;
import com.ims.institute.dto.InstituteResponse;
import com.ims.institute.dto.UpdateInstituteRequest;
import jakarta.validation.Valid;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/api/institutes")
public class InstituteController {

    private final InstituteService instituteService;

    public InstituteController(InstituteService instituteService) {
        this.instituteService = instituteService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public InstituteResponse create(@Valid @RequestBody CreateInstituteRequest request) {
        return instituteService.create(request);
    }

    @GetMapping
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public PageResponse<InstituteResponse> list(Pageable pageable) {
        return instituteService.list(pageable);
    }

    /** Current user's own institute. */
    @GetMapping("/me")
    public InstituteResponse mine(@AuthenticationPrincipal SecurityUser user) {
        if (user.getInstituteId() == null) {
            throw new AccessDeniedException("No institute bound to this user");
        }
        return instituteService.get(user.getInstituteId());
    }

    @GetMapping("/{id}")
    public InstituteResponse get(@PathVariable UUID id, @AuthenticationPrincipal SecurityUser user) {
        assertVisible(user, id);
        return instituteService.get(id);
    }

    @PutMapping("/{id}")
    public InstituteResponse update(@PathVariable UUID id,
                                    @Valid @RequestBody UpdateInstituteRequest request,
                                    @AuthenticationPrincipal SecurityUser user) {
        assertVisible(user, id);
        return instituteService.update(id, request);
    }

    private void assertVisible(SecurityUser user, UUID instituteId) {
        if (user.getRole() == Role.SUPER_ADMIN) {
            return;
        }
        if (!instituteId.equals(user.getInstituteId())) {
            throw new AccessDeniedException("Not your institute");
        }
    }
}
