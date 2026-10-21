package com.example.billing.repository;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import com.example.billing.domain.Invoice;
import com.example.billing.domain.InvoiceStatus;

public interface InvoiceRepository extends JpaRepository<Invoice, UUID> {

    Page<Invoice> findByTenantId(UUID tenantId, Pageable pageable);

    Page<Invoice> findByTenantIdAndStatus(UUID tenantId, InvoiceStatus status, Pageable pageable);

    Optional<Invoice> findByIdAndTenantId(UUID id, UUID tenantId);
}
