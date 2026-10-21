package com.example.billing.repository;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import com.example.billing.domain.Customer;

public interface CustomerRepository extends JpaRepository<Customer, UUID> {

    Page<Customer> findByTenantId(UUID tenantId, Pageable pageable);

    Optional<Customer> findByIdAndTenantId(UUID id, UUID tenantId);
}
