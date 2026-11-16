package com.example.billing.config;

import java.time.Duration;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/** The app refuses to start without a strong signing secret. */
@ConfigurationProperties(prefix = "app.security.jwt")
@Validated
public record JwtProperties(
        @NotBlank @Size(min = 32, message = "must be at least 32 characters") String secret,
        @NotNull Duration accessTtl,
        @NotNull Duration refreshTtl) {
}
