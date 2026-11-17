package com.example.billing.common;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.Set;

import org.junit.jupiter.api.Test;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import com.example.billing.exception.BadRequestException;

class PageRequestsTest {

    private static final Set<String> ALLOWED = Set.of("name", "createdAt");
    private static final Sort DEFAULT = Sort.by("name");

    @Test
    void capsPageSize() {
        Pageable result = PageRequests.sanitize(PageRequest.of(0, 5000), ALLOWED, DEFAULT);
        assertEquals(PageRequests.MAX_PAGE_SIZE, result.getPageSize());
    }

    @Test
    void usesDefaultSortWhenNoneGiven() {
        Pageable result = PageRequests.sanitize(PageRequest.of(2, 10), ALLOWED, DEFAULT);
        assertEquals(DEFAULT, result.getSort());
        assertEquals(2, result.getPageNumber());
    }

    @Test
    void rejectsUnknownSortField() {
        Pageable request = PageRequest.of(0, 10, Sort.by("passwordHash"));
        assertThrows(BadRequestException.class, () -> PageRequests.sanitize(request, ALLOWED, DEFAULT));
    }
}
