package com.example.billing.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.example.billing.domain.Customer;
import com.example.billing.dto.CustomerRequest;
import com.example.billing.dto.CustomerResponse;
import com.example.billing.exception.ConflictException;
import com.example.billing.exception.NotFoundException;
import com.example.billing.repository.CustomerRepository;
import com.example.billing.security.CurrentUser;

@ExtendWith(MockitoExtension.class)
class CustomerServiceTest {

    private final UUID tenantId = UUID.randomUUID();

    @Mock
    private CustomerRepository customers;

    @Mock
    private CurrentUser currentUser;

    @InjectMocks
    private CustomerService service;

    @BeforeEach
    void setUp() {
        lenient().when(currentUser.tenantId()).thenReturn(tenantId);
    }

    @Test
    void createUsesTenantFromTokenAndNormalizesEmail() {
        when(customers.existsByTenantIdAndEmail(tenantId, "ana@example.com")).thenReturn(false);
        when(customers.save(any(Customer.class))).thenAnswer(invocation -> invocation.getArgument(0));

        CustomerResponse response = service.create(new CustomerRequest("  Ana  ", " Ana@Example.com ", " ", null));

        ArgumentCaptor<Customer> saved = ArgumentCaptor.forClass(Customer.class);
        verify(customers).save(saved.capture());
        assertEquals(tenantId, saved.getValue().getTenantId());
        assertEquals("ana@example.com", response.email());
        assertEquals("Ana", response.name());
    }

    @Test
    void createRejectsDuplicateEmailInSameTenant() {
        when(customers.existsByTenantIdAndEmail(tenantId, "ana@example.com")).thenReturn(true);

        assertThrows(ConflictException.class,
            () -> service.create(new CustomerRequest("Ana", "ana@example.com", null, null)));
        verify(customers, never()).save(any());
    }

    @Test
    void getHidesCustomersOfOtherTenants() {
        UUID id = UUID.randomUUID();
        when(customers.findByIdAndTenantId(id, tenantId)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> service.get(id));
    }
}
