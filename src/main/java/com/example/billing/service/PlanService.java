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
import com.example.billing.domain.Plan;
import com.example.billing.dto.PageResponse;
import com.example.billing.dto.PlanRequest;
import com.example.billing.dto.PlanResponse;
import com.example.billing.exception.ConflictException;
import com.example.billing.exception.NotFoundException;
import com.example.billing.repository.PlanRepository;
import com.example.billing.security.CurrentUser;

@Service
public class PlanService {

    private static final Set<String> SORTABLE_FIELDS = Set.of("name", "price", "createdAt");
    private static final Sort DEFAULT_SORT = Sort.by("name");

    private final PlanRepository plans;
    private final CurrentUser currentUser;

    public PlanService(PlanRepository plans, CurrentUser currentUser) {
        this.plans = plans;
        this.currentUser = currentUser;
    }

    @Transactional(readOnly = true)
    public PageResponse<PlanResponse> list(Boolean active, Pageable pageable) {
        Pageable page = PageRequests.sanitize(pageable, SORTABLE_FIELDS, DEFAULT_SORT);
        UUID tenantId = currentUser.tenantId();
        Page<Plan> result = (active == null)
            ? plans.findByTenantId(tenantId, page)
            : plans.findByTenantIdAndActive(tenantId, active, page);
        return PageResponse.from(result, PlanResponse::from);
    }

    @Transactional(readOnly = true)
    public PlanResponse get(UUID id) {
        return PlanResponse.from(find(id));
    }

    @Transactional
    public PlanResponse create(PlanRequest request) {
        Plan plan = new Plan();
        plan.setTenantId(currentUser.tenantId());
        apply(plan, request);
        plan.setActive(request.active() == null || request.active());
        return PlanResponse.from(plans.save(plan));
    }

    @Transactional
    public PlanResponse update(UUID id, PlanRequest request) {
        Plan plan = find(id);
        apply(plan, request);
        if (request.active() != null) {
            plan.setActive(request.active());
        }
        return PlanResponse.from(plan);
    }

    @Transactional
    public void delete(UUID id) {
        Plan plan = find(id);
        try {
            plans.delete(plan);
            plans.flush();
        } catch (DataIntegrityViolationException e) {
            throw new ConflictException("Plan is used by subscriptions; deactivate it instead of deleting it");
        }
    }

    private Plan find(UUID id) {
        return plans.findByIdAndTenantId(id, currentUser.tenantId())
            .orElseThrow(() -> new NotFoundException("Plan not found"));
    }

    private static void apply(Plan plan, PlanRequest request) {
        plan.setName(request.name().trim());
        plan.setDescription(request.description() == null || request.description().isBlank()
            ? null : request.description().trim());
        plan.setPrice(request.price());
        plan.setCurrency(request.currency());
        plan.setBillingInterval(request.billingInterval());
    }
}
