package com.example.billing.service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.Instant;
import java.util.Base64;
import java.util.HexFormat;

import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import com.example.billing.domain.AppUser;
import com.example.billing.domain.RefreshToken;
import com.example.billing.dto.LoginRequest;
import com.example.billing.dto.TokenResponse;
import com.example.billing.repository.AppUserRepository;
import com.example.billing.repository.RefreshTokenRepository;

@Service
public class AuthService {

    private static final SecureRandom RANDOM = new SecureRandom();

    private final AppUserRepository users;
    private final RefreshTokenRepository refreshTokens;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public AuthService(AppUserRepository users, RefreshTokenRepository refreshTokens,
                       PasswordEncoder passwordEncoder, JwtService jwtService) {
        this.users = users;
        this.refreshTokens = refreshTokens;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
    }

    @Transactional
    public TokenResponse login(LoginRequest request) {
        AppUser user = users.findByEmail(request.email().trim().toLowerCase())
            .filter(AppUser::isActive)
            .filter(candidate -> passwordEncoder.matches(request.password(), candidate.getPasswordHash()))
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid email or password"));
        return issueTokens(user);
    }

    /**
     * Refresh tokens are rotated: each one can be used exactly once. Presenting an already-used token
     * is treated as theft and revokes every refresh token of that user. noRollbackFor keeps that
     * revocation even though we throw afterwards.
     */
    @Transactional(noRollbackFor = ResponseStatusException.class)
    public TokenResponse refresh(String rawToken) {
        RefreshToken stored = refreshTokens.findByTokenHash(hash(rawToken))
            .orElseThrow(AuthService::invalidToken);

        if (stored.isRevoked()) {
            refreshTokens.revokeAllForUser(stored.getUserId());
            throw invalidToken();
        }
        if (stored.getExpiresAt().isBefore(Instant.now())) {
            throw invalidToken();
        }

        AppUser user = users.findById(stored.getUserId())
            .filter(AppUser::isActive)
            .orElseThrow(AuthService::invalidToken);

        stored.setRevoked(true);
        return issueTokens(user);
    }

    @Transactional
    public void logout(String rawToken) {
        refreshTokens.findByTokenHash(hash(rawToken)).ifPresent(token -> token.setRevoked(true));
    }

    private TokenResponse issueTokens(AppUser user) {
        String rawRefreshToken = generateRawToken();

        RefreshToken refreshToken = new RefreshToken();
        refreshToken.setUserId(user.getId());
        refreshToken.setTokenHash(hash(rawRefreshToken));
        refreshToken.setExpiresAt(Instant.now().plus(jwtService.refreshTtl()));
        refreshTokens.save(refreshToken);

        return new TokenResponse(
            jwtService.createAccessToken(user), rawRefreshToken, "Bearer", jwtService.accessTtlSeconds());
    }

    private static ResponseStatusException invalidToken() {
        return new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid refresh token");
    }

    private static String generateRawToken() {
        byte[] bytes = new byte[32];
        RANDOM.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }

    private static String hash(String rawToken) {
        try {
            byte[] digest = MessageDigest.getInstance("SHA-256").digest(rawToken.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(digest);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 not available", e);
        }
    }
}
