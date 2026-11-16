package com.example.billing.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.billing.domain.AppUser;

public interface AppUserRepository extends JpaRepository<AppUser, UUID> {

    Optional<AppUser> findByEmail(String email);

    Optional<AppUser> findByIdAndTenantId(UUID id, UUID tenantId);

    List<AppUser> findByTenantIdOrderByFullName(UUID tenantId);
}
