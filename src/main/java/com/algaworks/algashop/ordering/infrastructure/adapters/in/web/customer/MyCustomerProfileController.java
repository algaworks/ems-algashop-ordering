package com.algaworks.algashop.ordering.infrastructure.adapters.in.web.customer;

import com.algaworks.algashop.ordering.core.application.security.SecurityChecks;
import com.algaworks.algashop.ordering.core.ports.in.customer.*;
import com.algaworks.algashop.ordering.infrastructure.config.security.SecurityAnnotations;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/customers/me")
@RequiredArgsConstructor
public class MyCustomerProfileController {

	private final ForManagingCustomers forManagingCustomers;
	private final ForQueryingCustomers forQueryingCustomers;

	private final SecurityChecks securityChecks;

	@PostMapping
	@ResponseStatus(HttpStatus.CREATED)
	@SecurityAnnotations.CanWriteCustomerProfile
	public CustomerOutput create(@RequestBody @Valid CustomerInput input) {
		input.setUserId(securityChecks.getAuthenticatedUserId());
		UUID customerId = forManagingCustomers.create(input);
		return forQueryingCustomers.findById(customerId);
	}

	@GetMapping
	@SecurityAnnotations.CanReadCustomerProfile
	public CustomerOutput load() {
		return forQueryingCustomers.findById(securityChecks.getAuthenticatedUserId());
	}

	@PutMapping
	@SecurityAnnotations.CanWriteCustomerProfile
	public CustomerOutput update(@RequestBody @Valid CustomerUpdateInput input) {
		forManagingCustomers.update(securityChecks.getAuthenticatedUserId(), input);
		return forQueryingCustomers.findById(securityChecks.getAuthenticatedUserId());
	}

	@DeleteMapping
	@ResponseStatus(HttpStatus.NO_CONTENT)
	@SecurityAnnotations.CanWriteCustomerProfile
	public void delete() {
		forManagingCustomers.archive(securityChecks.getAuthenticatedUserId());
	}
}
