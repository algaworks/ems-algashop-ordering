package com.algaworks.algashop.ordering.core.ports.in.checkout;

import com.algaworks.algashop.ordering.core.application.checkout.command.ProcessAcceptedCheckoutIntegrationCommand;

public interface ForProcessingCheckoutAccepted {
	void process(ProcessAcceptedCheckoutIntegrationCommand integrationCommand);
}
