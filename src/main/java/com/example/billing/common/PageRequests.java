package com.example.billing.common;

import java.util.Set;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import com.example.billing.exception.BadRequestException;

/** Keeps clients from requesting huge pages or sorting by columns we do not expose. */
public final class PageRequests {

    public static final int MAX_PAGE_SIZE = 100;

    private PageRequests() {
    }

    public static Pageable sanitize(Pageable pageable, Set<String> allowedSortFields, Sort defaultSort) {
        int size = Math.min(pageable.getPageSize(), MAX_PAGE_SIZE);

        Sort sort = pageable.getSort();
        if (sort.isUnsorted()) {
            sort = defaultSort;
        } else {
            for (Sort.Order order : sort) {
                if (!allowedSortFields.contains(order.getProperty())) {
                    throw new BadRequestException(
                        "Cannot sort by '" + order.getProperty() + "'. Allowed fields: " + allowedSortFields);
                }
            }
        }
        return PageRequest.of(pageable.getPageNumber(), size, sort);
    }
}
