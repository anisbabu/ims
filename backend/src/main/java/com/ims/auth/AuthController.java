package com.ims.auth;

import com.ims.auth.dto.LoginRequest;
import com.ims.auth.dto.MeResponse;
import com.ims.auth.dto.RefreshRequest;
import com.ims.auth.dto.TokenResponse;
import com.ims.auth.dto.UserDtos.ChangePassword;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;
    private final UserService userService;

    public AuthController(AuthService authService, UserService userService) {
        this.authService = authService;
        this.userService = userService;
    }

    @PostMapping("/login")
    public TokenResponse login(@Valid @RequestBody LoginRequest request) {
        return authService.login(request);
    }

    @PostMapping("/refresh")
    public TokenResponse refresh(@Valid @RequestBody RefreshRequest request) {
        return authService.refresh(request.refreshToken());
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout() {
        // Stateless JWT: client discards tokens. Endpoint exists for symmetry.
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/me")
    public MeResponse me(@AuthenticationPrincipal SecurityUser user) {
        return authService.me(user.getId());
    }

    @PostMapping("/change-password")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void changePassword(@AuthenticationPrincipal SecurityUser user,
                               @Valid @RequestBody ChangePassword req) {
        userService.changeOwnPassword(user.getId(), req.currentPassword(), req.newPassword());
    }

    /** Roles the current actor is allowed to assign (drives admin UI selects). */
    @GetMapping("/roles")
    public List<Role> assignableRoles(@AuthenticationPrincipal SecurityUser user) {
        if (user.getRole() == Role.SUPER_ADMIN) {
            return List.of(Role.values());
        }
        return List.of(Role.INSTITUTE_ADMIN, Role.TEACHER, Role.STUDENT, Role.GUARDIAN);
    }
}
