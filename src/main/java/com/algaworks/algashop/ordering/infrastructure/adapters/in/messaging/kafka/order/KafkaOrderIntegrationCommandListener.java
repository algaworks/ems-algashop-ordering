package com.algaworks.algashop.ordering.infrastructure.adapters.in.messaging.kafka.order;

import com.algaworks.algashop.ordering.core.application.checkout.command.ProcessAcceptedCheckoutIntegrationCommand;
import com.algaworks.algashop.ordering.core.ports.in.checkout.ForProcessingCheckoutAccepted;
import com.algaworks.algashop.ordering.core.ports.out.idempotency.ForGuardingIdempotency;
import com.algaworks.algashop.ordering.infrastructure.config.kafka.KafkaConfig;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaHandler;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
@Slf4j
@RequiredArgsConstructor
@KafkaListener(
		id = "ordering.order-commands",
		topics = "#{algaShopMessagingKafkaProperties.orderCommandTopicName}"
)
public class KafkaOrderIntegrationCommandListener {

	private final ForProcessingCheckoutAccepted forProcessingCheckoutAccepted;

	private final ForGuardingIdempotency forGuardingIdempotency;

	@KafkaHandler
	public void handle(@Payload ProcessAcceptedCheckoutIntegrationCommand integrationCommand,
	                   @Header(value = KafkaHeaders.RECEIVED_KEY, required = false) String messageKey,
	                   @Header(value = KafkaHeaders.RECEIVED_PARTITION, required = false) Integer partition,
	                   @Header(value = KafkaHeaders.OFFSET, required = false) Integer offset,
	                   @Header(value = KafkaConfig.IDEMPOTENCY_KEY_HEADER) byte[] rawIdempotencyKey) {
		logReceived(integrationCommand, messageKey, partition, offset);

		UUID idempotencyKey = UUID.fromString(new String(rawIdempotencyKey));
		forGuardingIdempotency.runOnce(idempotencyKey, ()-> forProcessingCheckoutAccepted.process(integrationCommand));

	}

	@KafkaHandler(isDefault = true)
	public void handle(@Payload Object integrationCommand,
	                   @Header(value = KafkaHeaders.RECEIVED_KEY, required = false) String messageKey,
	                   @Header(value = KafkaHeaders.RECEIVED_PARTITION, required = false) Integer partition,
	                   @Header(value = KafkaHeaders.OFFSET, required = false) Integer offset) {

		log.warn("OrderIntegrationCommand Ignored | key={} | partiton={} | offset={} | thread={}",
				messageKey, partition, offset, Thread.currentThread().getName());

		throw new IllegalArgumentException("Unsupported order command");
	}

	private void logReceived(Object object, String messageKey, Integer partition, Integer offset) {
		log.info("Received {} | key={} | partiton={} | offset={} | thread={}",
				object.getClass().getSimpleName(), messageKey, partition, offset,
				Thread.currentThread().getName());
	}

}
