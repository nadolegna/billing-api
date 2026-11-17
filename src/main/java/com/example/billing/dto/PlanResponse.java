package com.example.billing.dto;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

import com.example.billing.domain.BillingInterval;
import com.example.billing.domain.Plan;

public record PlanResponse(
        UUID id, String name, String description, BigDecimal price, String currency,
        BillingInterval billingInterval, boolean active, Instant createdAt) {

    public static PlanResponse from(Plan plan) {
        return new PlanResponse(
            plan.getId(), plan.getName(), plan.getDescription(), plan.getPrice(), plan.getCurrency(),
            plan.getBillingInterval(), plan.isActive(), plan.getCreatedAt());
    }
}
