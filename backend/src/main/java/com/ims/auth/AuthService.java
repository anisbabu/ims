package com.ims.auth;

import com.ims.auth.dto.LoginRequest;
import com.ims.auth.dto.MeResponse;
import com.ims.auth.dto.TokenResponse;
import com.ims.common.BadRequestException;
import com.ims.common.NotFoundException;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
public class AuthService {

    private final AuthenticationManager authenticationManager;
    private final UserRepository userRepository;
    private final JwtService jwtService;

    public AuthService(AuthenticationManager authenticationManager,
                       UserRepository userRepository,
                       JwtService jwtService) {
        this.authenticationManager = authenticationManager;
        this.userRepository = userRepository;
        this.jwtService = jwtService;
    }

    @Transactional(readOnly = true)
    public TokenResponse login(LoginRequest request) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.email(), request.password()));
        User user = userRepository.findByEmailIgnoreCase(request.email())
                .orElseThrow(() -> new BadCredentialsException("Invalid credentials"));
        return issue(user);
    }

    @Transactional(readOnly = true)
    public TokenResponse refresh(String refreshToken) {
        Claims claims;
        try {
            claims = jwtService.parse(refreshToken);
        } catch (JwtException ex) {
            throw new BadRequestException("Invalid refresh token");
        }
        if (!JwtService.TYPE_REFRESH.equals(jwtService.getType(claims))) {
            throw new BadRequestException("Not a refresh token");
        }
        UUID userId = jwtService.getUserId(claims);
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BadRequestException("Invalid refresh token"));
        if (user.getStatus() != UserStatus.ACTIVE) {
            throw new BadRequestException("User is disabled");
        }
        return issue(user);
    }

    @Transactional(readOnly = true)
    public MeResponse me(UUID userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("User not found"));
        return new MeResponse(user.getId(), user.getEmail(), user.getFullName(),
                user.getRole(), user.getInstituteId(), user.getProfileId());
    }

    private TokenResponse issue(User user) {
        String access = jwtService.generateAccess(user);
        String refresh = jwtService.generateRefresh(user);
        return TokenResponse.of(access, refresh, jwtService.getAccessTtl());
    }
}
