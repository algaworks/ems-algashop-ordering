package com.algaworks.algashop.ordering.infrastructure.adapters.in.messaging.kafka.product;

import com.algaworks.algashop.ordering.core.application.product.event.ProductDelistedIntegrationEvent;
import com.algaworks.algashop.ordering.core.application.product.event.ProductListedIntegrationEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaHandler;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

@Component
@Slf4j
@KafkaListener(topics = {"product-catalog.product.events"})
public class KafkaProductIntegrationEventListener {

	@KafkaHandler
	public void handle(@Payload ProductListedIntegrationEvent event,
			@Header(value = KafkaHeaders.RECEIVED_KEY) String messageKey) {
		log.info("Received " + event.getClass());
		log.info("MessageKey " + messageKey);
	}

	@KafkaHandler
	public void handle(@Payload ProductDelistedIntegrationEvent event,
			@Header(value = KafkaHeaders.RECEIVED_KEY) String messageKey) {
		log.info("Received " + event.getClass());
		log.info("MessageKey " + messageKey);
	}

}
