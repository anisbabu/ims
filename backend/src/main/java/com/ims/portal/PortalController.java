package com.ims.portal;

import com.ims.auth.SecurityUser;
import com.ims.portal.dto.PortalDtos.GuardianPortal;
import com.ims.portal.dto.PortalDtos.StudentPortal;
import com.ims.portal.dto.PortalDtos.TeacherPortal;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** Role-scoped portal aggregates; scope comes from the caller's linked profile. */
@RestController
@RequestMapping("/api/portal")
public class PortalController {

    private final PortalService portalService;

    public PortalController(PortalService portalService) {
        this.portalService = portalService;
    }

    @GetMapping("/student")
    @PreAuthorize("hasRole('STUDENT')")
    public StudentPortal student(@AuthenticationPrincipal SecurityUser me) {
        return portalService.studentPortal(me);
    }

    @GetMapping("/guardian")
    @PreAuthorize("hasRole('GUARDIAN')")
    public GuardianPortal guardian(@AuthenticationPrincipal SecurityUser me) {
        return portalService.guardianPortal(me);
    }

    @GetMapping("/teacher")
    @PreAuthorize("hasRole('TEACHER')")
    public TeacherPortal teacher(@AuthenticationPrincipal SecurityUser me) {
        return portalService.teacherPortal(me);
    }
}
