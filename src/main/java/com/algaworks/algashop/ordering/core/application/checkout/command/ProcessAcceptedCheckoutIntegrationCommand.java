package com.algaworks.algashop.ordering.core.application.checkout.command;

import com.algaworks.algashop.ordering.core.application.IntegrationCommand;
import lombok.*;

@Data
@ToString
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProcessAcceptedCheckoutIntegrationCommand
		implements IntegrationCommand {

	private OrderSnapshot order;

	@Override
	public String getAggregateId() {
		return order.orderId();
	}
}
