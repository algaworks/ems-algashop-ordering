package com.algaworks.algashop.ordering.infrastructure.adapters.in.web.customer;

import com.algaworks.algashop.ordering.core.ports.in.customer.*;
import com.algaworks.algashop.ordering.infrastructure.adapters.in.web.PageModel;
import com.algaworks.algashop.ordering.infrastructure.config.security.SecurityAnnotations.CanReadCustomers;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/customers")
@RequiredArgsConstructor
public class CustomerController {

    private final ForQueryingCustomers forQueryingCustomers;

    @CanReadCustomers
    @GetMapping
    public PageModel<CustomerSummaryOutput> findAll(CustomerFilter customerFilter) {
        return PageModel.of(forQueryingCustomers.filter(customerFilter));
    }

    @CanReadCustomers
    @GetMapping("/{customerId}")
    public CustomerOutput findById(@PathVariable UUID customerId) {
        return forQueryingCustomers.findById(customerId);
    }

}
