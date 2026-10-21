package com.example.billing.repository;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.billing.domain.Tenant;

public interface TenantRepository extends JpaRepository<Tenant, UUID> {
}
