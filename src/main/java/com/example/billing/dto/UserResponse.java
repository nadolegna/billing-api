package com.example.billing.dto;

import java.time.Instant;
import java.util.UUID;

import com.example.billing.domain.AppUser;
import com.example.billing.domain.UserRole;

public record UserResponse(UUID id, String email, String fullName, UserRole role, boolean active, Instant createdAt) {

    public static UserResponse from(AppUser user) {
        return new UserResponse(
            user.getId(), user.getEmail(), user.getFullName(), user.getRole(), user.isActive(), user.getCreatedAt());
    }
}
