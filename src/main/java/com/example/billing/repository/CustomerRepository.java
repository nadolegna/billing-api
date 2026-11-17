package com.example.billing.repository;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.example.billing.domain.Customer;

public interface CustomerRepository extends JpaRepository<Customer, UUID> {

    Page<Customer> findByTenantId(UUID tenantId, Pageable pageable);

    Optional<Customer> findByIdAndTenantId(UUID id, UUID tenantId);

    boolean existsByTenantIdAndEmail(UUID tenantId, String email);

    boolean existsByTenantIdAndEmailAndIdNot(UUID tenantId, String email, UUID id);

    @Query("""
        select c from Customer c
        where c.tenantId = :tenantId
          and (lower(c.name) like lower(concat('%', :search, '%'))
               or lower(c.email) like lower(concat('%', :search, '%')))
        """)
    Page<Customer> search(@Param("tenantId") UUID tenantId, @Param("search") String search, Pageable pageable);
}
