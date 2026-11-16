package com.example.billing.service;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.Duration;
import java.util.UUID;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

import org.junit.jupiter.api.Test;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder;

import com.example.billing.config.JwtProperties;
import com.example.billing.domain.AppUser;
import com.example.billing.domain.UserRole;
import com.nimbusds.jose.jwk.source.ImmutableSecret;

class JwtServiceTest {

    private static final String SECRET = "test-secret-test-secret-test-secret-123";

    @Test
    void accessTokenCarriesSubjectTenantAndRoles() {
        SecretKey key = new SecretKeySpec(SECRET.getBytes(UTF_8), "HmacSHA256");
        JwtEncoder encoder = new NimbusJwtEncoder(new ImmutableSecret<>(key));
        JwtDecoder decoder = NimbusJwtDecoder.withSecretKey(key).macAlgorithm(MacAlgorithm.HS256).build();
        JwtService service = new JwtService(
            encoder, new JwtProperties(SECRET, Duration.ofMinutes(15), Duration.ofDays(7)));

        AppUser user = new AppUser();
        user.setId(UUID.randomUUID());
        user.setTenantId(UUID.randomUUID());
        user.setEmail("admin@acme.example");
        user.setRole(UserRole.ADMIN);

        Jwt jwt = decoder.decode(service.createAccessToken(user));

        assertEquals(user.getId().toString(), jwt.getSubject());
        assertEquals(user.getTenantId().toString(), jwt.getClaimAsString("tenant_id"));
        assertTrue(jwt.getClaimAsStringList("roles").contains("ADMIN"));
        assertEquals(900, service.accessTtlSeconds());
    }
}
