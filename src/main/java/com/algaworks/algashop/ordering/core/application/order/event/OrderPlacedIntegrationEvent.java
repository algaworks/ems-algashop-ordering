package com.algaworks.algashop.ordering.core.application.order.event;

import com.algaworks.algashop.ordering.core.application.IntegrationEvent;
import com.algaworks.algashop.ordering.core.domain.model.IdGenerator;
import lombok.*;

import java.time.OffsetDateTime;
import java.util.UUID;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class OrderPlacedIntegrationEvent implements IntegrationEvent {
	private UUID idempotencyKey = IdGenerator.generateTimeBasedUUID();

	private String orderId;
	private UUID customerId;
	private OffsetDateTime placedAt;

	public OrderPlacedIntegrationEvent(String orderId, UUID customerId, OffsetDateTime placedAt) {
		this.placedAt = placedAt;
		this.customerId = customerId;
		this.orderId = orderId;
	}

	@Override
	public String getAggregateId() {
		return orderId;
	}
}
