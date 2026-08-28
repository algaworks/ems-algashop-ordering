package com.algaworks.algashop.ordering.core.application.checkout;

import com.algaworks.algashop.ordering.core.application.order.event.CheckoutAcceptedIntegrationEvent;
import com.algaworks.algashop.ordering.core.application.order.event.OrderPlacedIntegrationEvent;
import com.algaworks.algashop.ordering.core.application.order.event.OrderSnapshotAssembler;
import com.algaworks.algashop.ordering.core.domain.model.order.Order;
import com.algaworks.algashop.ordering.core.domain.model.order.OrderId;
import com.algaworks.algashop.ordering.core.domain.model.order.Orders;
import com.algaworks.algashop.ordering.core.domain.model.shoppingcart.ShoppingCartId;
import com.algaworks.algashop.ordering.core.domain.model.shoppingcart.ShoppingCarts;
import com.algaworks.algashop.ordering.core.ports.in.checkout.ForProcessingCheckoutAccepted;
import com.algaworks.algashop.ordering.core.ports.out.order.ForPublishingOrderIntegrationEvents;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@Slf4j
@RequiredArgsConstructor
public class ProcessAcceptedCheckoutApplicationService implements ForProcessingCheckoutAccepted {

	private final Orders orders;
	private final ShoppingCarts shoppingCarts;
	private final OrderSnapshotAssembler orderSnapshotAssembler;
	private final ForPublishingOrderIntegrationEvents forPublishingOrderIntegrationEvents;

	@Override
	@Transactional
	public void process(CheckoutAcceptedIntegrationEvent event) {
		OrderId orderId = new OrderId(event.getAggregateId());

		if (orders.exists(orderId)) {
			log.info("Checkout command ignored order {} already exists", orderId);
			return;
		}

		Order order = orderSnapshotAssembler.toDomain(event.getOrder());
		orders.add(order);

		UUID rawShoppingCartId = event.getOrder().shoppingCartId();
		if(rawShoppingCartId != null) {
			shoppingCarts.ofId(new ShoppingCartId(rawShoppingCartId)).ifPresent(shoppingCart -> {
				shoppingCart.empty();
				shoppingCarts.add(shoppingCart);
			});
		}

		forPublishingOrderIntegrationEvents.send(
				new OrderPlacedIntegrationEvent(
						orderId.toString(),
						order.customerId().value(),
						order.placedAt()
				)
		);
	}

}
