package com.algaworks.algashop.ordering.core.ports.out.order;

import com.algaworks.algashop.ordering.core.application.IntegrationEvent;

public interface ForPublishingOrderIntegrationEvents {
	void send(IntegrationEvent event);
}
