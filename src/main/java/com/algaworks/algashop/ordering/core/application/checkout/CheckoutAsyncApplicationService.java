package com.algaworks.algashop.ordering.core.application.checkout;

import com.algaworks.algashop.ordering.core.application.order.BillingInputDisassembler;
import com.algaworks.algashop.ordering.core.application.order.ShippingInputDisassembler;
import com.algaworks.algashop.ordering.core.application.order.event.CheckoutAcceptedIntegrationEvent;
import com.algaworks.algashop.ordering.core.application.order.event.OrderSnapshot;
import com.algaworks.algashop.ordering.core.application.order.event.OrderSnapshotAssembler;
import com.algaworks.algashop.ordering.core.application.security.SecurityChecks;
import com.algaworks.algashop.ordering.core.domain.model.DomainException;
import com.algaworks.algashop.ordering.core.domain.model.commons.ZipCode;
import com.algaworks.algashop.ordering.core.domain.model.customer.Customer;
import com.algaworks.algashop.ordering.core.domain.model.customer.CustomerId;
import com.algaworks.algashop.ordering.core.domain.model.customer.CustomerNotFoundException;
import com.algaworks.algashop.ordering.core.domain.model.customer.Customers;
import com.algaworks.algashop.ordering.core.domain.model.order.*;
import com.algaworks.algashop.ordering.core.domain.model.order.shipping.OriginAddressService;
import com.algaworks.algashop.ordering.core.domain.model.order.shipping.ShippingCostService;
import com.algaworks.algashop.ordering.core.domain.model.product.Product;
import com.algaworks.algashop.ordering.core.domain.model.product.ProductCatalogService;
import com.algaworks.algashop.ordering.core.domain.model.product.ProductId;
import com.algaworks.algashop.ordering.core.domain.model.product.ProductNotFoundException;
import com.algaworks.algashop.ordering.core.domain.model.shoppingcart.ShoppingCart;
import com.algaworks.algashop.ordering.core.domain.model.shoppingcart.ShoppingCartNotFoundException;
import com.algaworks.algashop.ordering.core.domain.model.shoppingcart.ShoppingCarts;
import com.algaworks.algashop.ordering.core.ports.in.checkout.CheckoutInput;
import com.algaworks.algashop.ordering.core.ports.in.checkout.ForBuyingWithShoppingCart;
import com.algaworks.algashop.ordering.core.ports.in.checkout.ForBuyingWithShoppingCartAsync;
import com.algaworks.algashop.ordering.core.ports.in.checkout.ShippingInput;
import com.algaworks.algashop.ordering.core.ports.out.order.ForPublishingOrderIntegrationEvents;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Objects;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CheckoutAsyncApplicationService implements ForBuyingWithShoppingCartAsync {

	private final ShoppingCarts shoppingCarts;
	private final Customers customers;

	private final CheckoutService checkoutService;

	private final BillingInputDisassembler billingInputDisassembler;
	private final ShippingInputDisassembler shippingInputDisassembler;

	private final ShippingCostService shippingCostService;
	private final OriginAddressService originAddressService;
	private final ProductCatalogService productCatalogService;

	private final SecurityChecks securityCheck;

	private final OrderSnapshotAssembler orderSnapshotAssembler;
	private final ForPublishingOrderIntegrationEvents forPublishingOrderIntegrationEvents;

	@Transactional
	public String checkout(CheckoutInput input) {
		Objects.requireNonNull(input);

		PaymentMethod paymentMethod = PaymentMethod.valueOf(input.getPaymentMethod());
		CustomerId customerId = new CustomerId(input.getCustomerId());
		CreditCardId creditCardId = getCreditCardId(input, paymentMethod);

		Customer customer = loadCustomer(customerId);
		ShoppingCart shoppingCart = loadShoppingCart(customerId);

		verifyCanOrderFor(shoppingCart.customerId().value());

		var shippingCalculationResult = calculateShippingCost(input.getShipping());

		Order order = checkoutService.checkout(customer, shoppingCart,
				billingInputDisassembler.toDomainModel(input.getBilling()),
				shippingInputDisassembler.toDomainModel(input.getShipping(), shippingCalculationResult),
				paymentMethod, creditCardId);

		OrderSnapshot snapshot = orderSnapshotAssembler.toSnapshot(order, shoppingCart.id().value());

		CheckoutAcceptedIntegrationEvent integrationEvent = new CheckoutAcceptedIntegrationEvent(snapshot);
		forPublishingOrderIntegrationEvents.send(integrationEvent);

		return order.id().toString();
	}

	private ShoppingCart loadShoppingCart(CustomerId customerId) {
		return shoppingCarts.ofCustomer(customerId).orElseThrow(() -> ShoppingCartNotFoundException.ofCustomer(customerId.value()));
	}

	private Customer loadCustomer(CustomerId customerId) {
		return customers.ofId(customerId).orElseThrow(() -> new CustomerNotFoundException(customerId));
	}

	private CreditCardId getCreditCardId(CheckoutInput input, PaymentMethod paymentMethod) {
		CreditCardId creditCardId = null;

		if (paymentMethod.equals(PaymentMethod.CREDIT_CARD)) {
			if (input.getCreditCardId() == null) {
				throw new DomainException("Credit card id is required");
			}
			creditCardId = new CreditCardId(input.getCreditCardId());
		}

		return creditCardId;
	}

	private ShippingCostService.CalculationResult calculateShippingCost(ShippingInput shipping) {
		ZipCode origin = originAddressService.originAddress().zipCode();
		ZipCode destination = new ZipCode(shipping.getAddress().getZipCode());
		return shippingCostService.calculate(new ShippingCostService.CalculationRequest(origin, destination));
	}

	private Product findProduct(ProductId productId) {
		return productCatalogService.ofId(productId)
				.orElseThrow(()-> new ProductNotFoundException());
	}

	private void verifyCanOrderFor(@NotNull UUID customerId) {
		if (!securityCheck.canOrderFor(customerId)) {
			throw new AccessDeniedException("Cannot order for customer " + customerId);
		}
	}

}