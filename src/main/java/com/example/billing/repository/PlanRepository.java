package com.example.billing.repository;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import com.example.billing.domain.Plan;

public interface PlanRepository extends JpaRepository<Plan, UUID> {

    Page<Plan> findByTenantId(UUID tenantId, Pageable pageable);

    Optional<Plan> findByIdAndTenantId(UUID id, UUID tenantId);
}
