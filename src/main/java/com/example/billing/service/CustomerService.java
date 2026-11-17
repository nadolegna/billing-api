package com.example.billing.service;

import java.util.Set;
import java.util.UUID;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.billing.common.PageRequests;
import com.example.billing.domain.Customer;
import com.example.billing.dto.CustomerRequest;
import com.example.billing.dto.CustomerResponse;
import com.example.billing.dto.PageResponse;
import com.example.billing.exception.ConflictException;
import com.example.billing.exception.NotFoundException;
import com.example.billing.repository.CustomerRepository;
import com.example.billing.security.CurrentUser;

@Service
public class CustomerService {

    private static final Set<String> SORTABLE_FIELDS = Set.of("name", "email", "createdAt");
    private static final Sort DEFAULT_SORT = Sort.by("name");

    private final CustomerRepository customers;
    private final CurrentUser currentUser;

    public CustomerService(CustomerRepository customers, CurrentUser currentUser) {
        this.customers = customers;
        this.currentUser = currentUser;
    }

    @Transactional(readOnly = true)
    public PageResponse<CustomerResponse> list(String search, Pageable pageable) {
        Pageable page = PageRequests.sanitize(pageable, SORTABLE_FIELDS, DEFAULT_SORT);
        UUID tenantId = currentUser.tenantId();
        Page<Customer> result = (search == null || search.isBlank())
            ? customers.findByTenantId(tenantId, page)
            : customers.search(tenantId, search.trim(), page);
        return PageResponse.from(result, CustomerResponse::from);
    }

    @Transactional(readOnly = true)
    public CustomerResponse get(UUID id) {
        return CustomerResponse.from(find(id));
    }

    @Transactional
    public CustomerResponse create(CustomerRequest request) {
        UUID tenantId = currentUser.tenantId();
        String email = normalizeEmail(request.email());
        if (customers.existsByTenantIdAndEmail(tenantId, email)) {
            throw new ConflictException("A customer with this email already exists");
        }

        Customer customer = new Customer();
        customer.setTenantId(tenantId); // from the token, never from the request body
        apply(customer, request, email);
        return CustomerResponse.from(customers.save(customer));
    }

    @Transactional
    public CustomerResponse update(UUID id, CustomerRequest request) {
        Customer customer = find(id);
        String email = normalizeEmail(request.email());
        if (customers.existsByTenantIdAndEmailAndIdNot(customer.getTenantId(), email, id)) {
            throw new ConflictException("A customer with this email already exists");
        }
        apply(customer, request, email);
        return CustomerResponse.from(customer);
    }

    @Transactional
    public void delete(UUID id) {
        Customer customer = find(id);
        try {
            customers.delete(customer);
            customers.flush(); // surface foreign-key violations here instead of at commit
        } catch (DataIntegrityViolationException e) {
            throw new ConflictException("Customer has subscriptions or invoices and cannot be deleted");
        }
    }

    private Customer find(UUID id) {
        return customers.findByIdAndTenantId(id, currentUser.tenantId())
            .orElseThrow(() -> new NotFoundException("Customer not found"));
    }

    private static void apply(Customer customer, CustomerRequest request, String email) {
        customer.setName(request.name().trim());
        customer.setEmail(email);
        customer.setPhone(blankToNull(request.phone()));
        customer.setAddress(blankToNull(request.address()));
    }

    private static String normalizeEmail(String email) {
        return email.trim().toLowerCase();
    }

    private static String blankToNull(String value) {
        return (value == null || value.isBlank()) ? null : value.trim();
    }
}
