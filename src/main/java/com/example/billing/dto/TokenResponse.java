package com.example.billing.dto;

public record TokenResponse(String accessToken, String refreshToken, String tokenType, long expiresIn) {
}
