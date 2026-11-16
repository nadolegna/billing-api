package com.example.billing.security;

import java.util.UUID;

import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Component;

/**
 * The single place that answers "who is calling and which tenant do they belong to?".
 * Services use tenantId() for every query, never a tenant id supplied by the client.
 */
@Component
public class CurrentUser {

    public UUID userId() {
        return UUID.fromString(jwt().getSubject());
    }

    public UUID tenantId() {
        return UUID.fromString(jwt().getClaimAsString("tenant_id"));
    }

    private Jwt jwt() {
        var authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication instanceof JwtAuthenticationToken token) {
            return token.getToken();
        }
        throw new IllegalStateException("No authenticated JWT in the security context");
    }
}
