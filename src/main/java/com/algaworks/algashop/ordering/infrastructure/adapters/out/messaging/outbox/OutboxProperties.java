package com.algaworks.algashop.ordering.infrastructure.adapters.out.messaging.outbox;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@Data
@ConfigurationProperties("algashop.messaging.outbox")
public class OutboxProperties {
	private boolean enabled = false;

	private Dispatcher dispatcher = new Dispatcher();

	@Data
	public static class Dispatcher {
		private boolean enabled = false;
	}
}
