package com.algaworks.algashop.ordering.infrastructure.adapters.in.web.order;

import com.algaworks.algashop.ordering.core.application.security.SecurityChecks;
import com.algaworks.algashop.ordering.core.domain.model.customer.CustomerNotFoundException;
import com.algaworks.algashop.ordering.core.domain.model.product.ProductNotFoundException;
import com.algaworks.algashop.ordering.core.domain.model.shoppingcart.ShoppingCartNotFoundException;
import com.algaworks.algashop.ordering.core.ports.in.checkout.*;
import com.algaworks.algashop.ordering.core.ports.in.order.ForQueryingOrders;
import com.algaworks.algashop.ordering.core.ports.in.order.OrderFilter;
import com.algaworks.algashop.ordering.core.ports.out.order.OrderDetailOutput;
import com.algaworks.algashop.ordering.core.ports.out.order.OrderSummaryOutput;
import com.algaworks.algashop.ordering.infrastructure.adapters.in.web.PageModel;
import com.algaworks.algashop.ordering.infrastructure.adapters.in.web.exceptionhandler.UnprocessableEntityException;
import com.algaworks.algashop.ordering.infrastructure.config.security.SecurityAnnotations;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping(path = "/api/v2/customers/me/orders")
@RequiredArgsConstructor
public class MyOrdersV2Controller {

    private final ForBuyingWithShoppingCartAsync forBuyingWithShoppingCart;
    private final ForBuyingProductAsync forBuyingProduct;
    private final SecurityChecks securityChecks;

    @SneakyThrows
    @SecurityAnnotations.CanWriteMyOrders
    @PostMapping(consumes = "application/vnd.order-with-product.v2+json")
    @ResponseStatus(HttpStatus.ACCEPTED)
    public OrderAcceptedOutput createWithProduct(@Valid @RequestBody BuyNowInput input) {
        input.setCustomerId(securityChecks.getAuthenticatedUserId());
        String orderId;
        try {
            orderId = forBuyingProduct.buyNow(input);
        } catch (CustomerNotFoundException | ProductNotFoundException e) {
            throw new UnprocessableEntityException(e.getMessage(), e);
        }
        return new OrderAcceptedOutput(orderId);
    }

    @SecurityAnnotations.CanWriteMyOrders
    @PostMapping(consumes = "application/vnd.order-with-shopping-cart.v2+json")
    @ResponseStatus(HttpStatus.ACCEPTED)
    public OrderAcceptedOutput createWithShoppingCart(@Valid @RequestBody CheckoutInput input) {
        input.setCustomerId(securityChecks.getAuthenticatedUserId());
        String orderId;
        try {
            orderId = forBuyingWithShoppingCart.checkout(input);
        } catch (CustomerNotFoundException | ShoppingCartNotFoundException e) {
            throw new UnprocessableEntityException(e.getMessage(), e);
        }
        return new OrderAcceptedOutput(orderId);
    }

}
