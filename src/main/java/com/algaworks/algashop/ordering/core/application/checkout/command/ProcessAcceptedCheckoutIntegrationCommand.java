package com.algaworks.algashop.ordering.core.application.checkout.command;

import com.algaworks.algashop.ordering.core.application.IntegrationCommand;
import com.algaworks.algashop.ordering.core.domain.model.IdGenerator;
import lombok.*;

import java.util.UUID;

@Data
@ToString
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProcessAcceptedCheckoutIntegrationCommand
		implements IntegrationCommand {
	private UUID idempotencyKey = IdGenerator.generateTimeBasedUUID();
	private OrderSnapshot order;

	public ProcessAcceptedCheckoutIntegrationCommand(OrderSnapshot order) {
		this.order = order;
	}

	@Override
	public String getAggregateId() {
		return order.orderId();
	}

	@Override
	public UUID getIdempotencyKey() {
		return idempotencyKey;
	}
}
