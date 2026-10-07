package com.algaworks.algashop.ordering.core.ports.in.order.saga;

import java.util.UUID;

public interface ForCoordinatingPlaceOrderSaga {
	void start(String rawOrderId);
	void onInvoicePaid(UUID sagaId);
}
