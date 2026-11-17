package com.example.billing.service;

import java.util.List;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.billing.domain.AppUser;
import com.example.billing.dto.CreateUserRequest;
import com.example.billing.dto.UserResponse;
import com.example.billing.exception.ConflictException;
import com.example.billing.exception.NotFoundException;
import com.example.billing.repository.AppUserRepository;
import com.example.billing.security.CurrentUser;

@Service
public class UserService {

    private final AppUserRepository users;
    private final PasswordEncoder passwordEncoder;
    private final CurrentUser currentUser;

    public UserService(AppUserRepository users, PasswordEncoder passwordEncoder, CurrentUser currentUser) {
        this.users = users;
        this.passwordEncoder = passwordEncoder;
        this.currentUser = currentUser;
    }

    @Transactional(readOnly = true)
    public UserResponse me() {
        return users.findByIdAndTenantId(currentUser.userId(), currentUser.tenantId())
            .map(UserResponse::from)
            .orElseThrow(() -> new NotFoundException("User not found"));
    }

    @Transactional(readOnly = true)
    public List<UserResponse> list() {
        return users.findByTenantIdOrderByFullName(currentUser.tenantId()).stream()
            .map(UserResponse::from)
            .toList();
    }

    @Transactional
    public UserResponse create(CreateUserRequest request) {
        String email = request.email().trim().toLowerCase();
        if (users.findByEmail(email).isPresent()) {
            throw new ConflictException("Email already in use");
        }

        AppUser user = new AppUser();
        user.setTenantId(currentUser.tenantId()); // always the caller's tenant, never from the request
        user.setEmail(email);
        user.setPasswordHash(passwordEncoder.encode(request.password()));
        user.setFullName(request.fullName().trim());
        user.setRole(request.role());
        return UserResponse.from(users.save(user));
    }
}
