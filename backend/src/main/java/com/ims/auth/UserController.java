package com.ims.auth;

import com.ims.auth.dto.UserDtos.CreateUser;
import com.ims.auth.dto.UserDtos.ResetPassword;
import com.ims.auth.dto.UserDtos.UpdateUser;
import com.ims.auth.dto.UserDtos.UserResponse;
import com.ims.common.PageResponse;
import jakarta.validation.Valid;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/api/users")
@PreAuthorize("hasAnyRole('SUPER_ADMIN','INSTITUTE_ADMIN')")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public UserResponse create(@AuthenticationPrincipal SecurityUser actor,
                               @Valid @RequestBody CreateUser req) {
        return userService.create(actor, req);
    }

    @GetMapping
    public PageResponse<UserResponse> list(@AuthenticationPrincipal SecurityUser actor,
                                           @RequestParam(required = false) Role role,
                                           Pageable pageable) {
        return userService.list(actor, role, pageable);
    }

    @GetMapping("/{id}")
    public UserResponse get(@AuthenticationPrincipal SecurityUser actor, @PathVariable UUID id) {
        return userService.get(actor, id);
    }

    @PutMapping("/{id}")
    public UserResponse update(@AuthenticationPrincipal SecurityUser actor,
                               @PathVariable UUID id, @Valid @RequestBody UpdateUser req) {
        return userService.update(actor, id, req);
    }

    @PatchMapping("/{id}/password")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void resetPassword(@AuthenticationPrincipal SecurityUser actor,
                              @PathVariable UUID id, @Valid @RequestBody ResetPassword req) {
        userService.resetPassword(actor, id, req.newPassword());
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@AuthenticationPrincipal SecurityUser actor, @PathVariable UUID id) {
        userService.delete(actor, id);
    }
}
