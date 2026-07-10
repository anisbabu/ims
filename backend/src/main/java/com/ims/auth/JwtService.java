package com.ims.auth;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;
import java.util.UUID;

@Service
public class JwtService {

    public static final String TYPE_ACCESS = "access";
    public static final String TYPE_REFRESH = "refresh";

    private final SecretKey key;
    private final long accessTtl;
    private final long refreshTtl;

    public JwtService(
            @Value("${ims.jwt.secret}") String secret,
            @Value("${ims.jwt.access-ttl-seconds}") long accessTtl,
            @Value("${ims.jwt.refresh-ttl-seconds}") long refreshTtl) {
        this.key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        this.accessTtl = accessTtl;
        this.refreshTtl = refreshTtl;
    }

    public String generateAccess(User user) {
        return build(user, TYPE_ACCESS, accessTtl);
    }

    public String generateRefresh(User user) {
        return build(user, TYPE_REFRESH, refreshTtl);
    }

    public long getAccessTtl() {
        return accessTtl;
    }

    public long getRefreshTtl() {
        return refreshTtl;
    }

    private String build(User user, String type, long ttlSeconds) {
        Instant now = Instant.now();
        var builder = Jwts.builder()
                .subject(user.getId().toString())
                .claim("email", user.getEmail())
                .claim("role", user.getRole().name())
                .claim("type", type)
                .issuedAt(Date.from(now))
                .expiration(Date.from(now.plusSeconds(ttlSeconds)));
        if (user.getInstituteId() != null) {
            builder.claim("institute_id", user.getInstituteId().toString());
        }
        return builder.signWith(key).compact();
    }

    public Claims parse(String token) {
        return Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    public UUID getUserId(Claims claims) {
        return UUID.fromString(claims.getSubject());
    }

    public UUID getInstituteId(Claims claims) {
        String v = claims.get("institute_id", String.class);
        return v == null ? null : UUID.fromString(v);
    }

    public Role getRole(Claims claims) {
        return Role.valueOf(claims.get("role", String.class));
    }

    public String getType(Claims claims) {
        return claims.get("type", String.class);
    }
}
