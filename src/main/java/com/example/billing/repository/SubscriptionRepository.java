package com.example.billing.repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.billing.domain.Subscription;
import com.example.billing.domain.SubscriptionStatus;

public interface SubscriptionRepository extends JpaRepository<Subscription, UUID> {

    Optional<Subscription> findByIdAndTenantId(UUID id, UUID tenantId);

    // Used by the recurring-billing job on Day 6.
    List<Subscription> findByStatusAndNextBillingDateLessThanEqual(SubscriptionStatus status, LocalDate date);
}
