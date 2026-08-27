package com.algaworks.algashop.ordering.core.application.order.event;

import com.algaworks.algashop.ordering.core.application.IntegrationEvent;
import lombok.*;

@Data
@ToString
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CheckoutAcceptedIntegrationEvent implements IntegrationEvent {

	private OrderSnapshot order;

	@Override
	public String getAggregateId() {
		return order.orderId();
	}
}
