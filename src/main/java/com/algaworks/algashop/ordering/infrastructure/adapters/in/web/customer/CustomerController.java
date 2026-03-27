package com.algaworks.algashop.ordering.infrastructure.adapters.in.web.customer;

import com.algaworks.algashop.ordering.core.ports.in.customer.*;
import com.algaworks.algashop.ordering.core.ports.in.shoppingcart.ForQueryingShoppingCarts;
import com.algaworks.algashop.ordering.core.ports.in.shoppingcart.ShoppingCartOutput;
import com.algaworks.algashop.ordering.infrastructure.adapters.in.web.PageModel;
import com.algaworks.algashop.ordering.infrastructure.config.security.SecurityAnnotations.CanReadCustomers;
import com.algaworks.algashop.ordering.infrastructure.config.security.SecurityAnnotations.CanReadShoppingCarts;
import com.algaworks.algashop.ordering.infrastructure.config.security.SecurityAnnotations.CanWriteCustomers;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.UUID;

import static org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder.fromMethodCall;
import static org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder.on;

@RestController
@RequestMapping("/api/v1/customers")
@RequiredArgsConstructor
public class CustomerController {

    private final ForManagingCustomers forManagingCustomers;
    private final ForQueryingCustomers forQueryingCustomers;
    private final ForQueryingShoppingCarts forQueryingShoppingCarts;

    @CanWriteCustomers
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public CustomerOutput create(@RequestBody @Valid CustomerInput input, HttpServletResponse httpServletResponse) {
        UUID customerId = forManagingCustomers.create(input);

        UriComponentsBuilder builder = fromMethodCall(on(CustomerController.class).findById(customerId));
        httpServletResponse.addHeader("Location", builder.toUriString());

        return forQueryingCustomers.findById(customerId);
    }

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

    @CanReadShoppingCarts
    @GetMapping("/{customerId}/shopping-cart")
    public ShoppingCartOutput findShoppingCartByCustomerId(@PathVariable UUID customerId) {
        return forQueryingShoppingCarts.findByCustomerId(customerId);
    }

    @CanWriteCustomers
    @PutMapping("/{customerId}")
    public CustomerOutput update(@PathVariable UUID customerId,
                                 @RequestBody @Valid CustomerUpdateInput input) {
        forManagingCustomers.update(customerId, input);
        return forQueryingCustomers.findById(customerId);
    }

    @CanWriteCustomers
    @DeleteMapping("/{customerId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable UUID customerId) {
        forManagingCustomers.archive(customerId);
    }

}
