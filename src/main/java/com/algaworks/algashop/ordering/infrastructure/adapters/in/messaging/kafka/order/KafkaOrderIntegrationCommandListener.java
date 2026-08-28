package com.algaworks.algashop.ordering.infrastructure.adapters.in.messaging.kafka.order;

import com.algaworks.algashop.ordering.core.application.checkout.command.ProcessAcceptedCheckoutIntegrationCommand;
import com.algaworks.algashop.ordering.core.ports.in.checkout.ForProcessingCheckoutAccepted;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaHandler;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

@Component
@Slf4j
@RequiredArgsConstructor
@KafkaListener(
		id = "ordering.order-commands",
		topics = "#{algaShopMessagingKafkaProperties.orderCommandTopicName}"
)
public class KafkaOrderIntegrationCommandListener {

	private final ForProcessingCheckoutAccepted forProcessingCheckoutAccepted;

	@KafkaHandler
	public void handle(@Payload ProcessAcceptedCheckoutIntegrationCommand integrationCommand,
	                   @Header(value = KafkaHeaders.RECEIVED_KEY, required = false) String messageKey,
	                   @Header(value = KafkaHeaders.RECEIVED_PARTITION, required = false) Integer partition,
	                   @Header(value = KafkaHeaders.OFFSET, required = false) Integer offset) {
		logReceived(integrationCommand, messageKey, partition, offset);
		forProcessingCheckoutAccepted.process(integrationCommand);
	}

	@KafkaHandler(isDefault = true)
	public void handle(Object integrationCommand) {
		log.info("Order integration command ignored: {}", integrationCommand.getClass().getSimpleName());
	}

	private void logReceived(Object event, String messageKey, Integer partition, Integer offset) {
		log.info("Received {} | key={} | partiton={} | offset={} | thread={}",
				event.getClass().getSimpleName(), messageKey, partition, offset,
				Thread.currentThread().getName());
	}

}
