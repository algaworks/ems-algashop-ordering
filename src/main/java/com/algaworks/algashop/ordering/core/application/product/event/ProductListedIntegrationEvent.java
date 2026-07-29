package com.algaworks.algashop.ordering.core.application.product.event;

import com.algaworks.algashop.ordering.core.application.IntegrationEvent;
import lombok.*;

import java.time.OffsetDateTime;
import java.util.UUID;

@Getter
@Setter
@ToString
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductListedIntegrationEvent implements IntegrationEvent {
	private UUID productId;
	private OffsetDateTime listedAt;

	@Override
	public String getAggregateId() {
		return productId.toString();
	}
}