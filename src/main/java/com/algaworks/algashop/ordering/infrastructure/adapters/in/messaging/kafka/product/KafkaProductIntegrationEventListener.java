package com.algaworks.algashop.ordering.infrastructure.adapters.in.messaging.kafka.product;

import com.algaworks.algashop.ordering.core.application.product.event.ProductDelistedIntegrationEvent;
import com.algaworks.algashop.ordering.core.application.product.event.ProductListedIntegrationEvent;
import com.algaworks.algashop.ordering.core.application.product.event.ProductPriceChangedV2IntegrationEvent;
import com.algaworks.algashop.ordering.core.domain.model.DomainException;
import com.algaworks.algashop.ordering.core.ports.in.shoppingcart.ForManagingShoppingCarts;
import com.algaworks.algashop.ordering.infrastructure.config.cache.ProductCacheManager;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.annotation.KafkaHandler;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

import java.time.Duration;

@Component
@Slf4j
@KafkaListener(
		id = "ordering.product-events",
		idIsGroup = false,
		concurrency = "3",
		topics = {"#{algaShopMessagingKafkaProperties.productEventTopicName}"}
)
@RequiredArgsConstructor
public class KafkaProductIntegrationEventListener {

	private final ForManagingShoppingCarts forManagingShoppingCarts;
	private final ProductCacheManager productCacheManager;

	@Value("${simulate:none}") // none | slow | technical | business
	private String simulate;

	@KafkaHandler(isDefault = true)
	public void handle(@Payload Object event,
	                   @Header(value = KafkaHeaders.RECEIVED_KEY, required = false) String messageKey,
	                   @Header(value = KafkaHeaders.OFFSET, required = false) String messageOffset
	                   ) {
		log.info("Event ignored: key={} offset={}", messageKey, messageOffset);
//		simulateProcessing();
	}

	@KafkaHandler
	public void handle(@Payload ProductListedIntegrationEvent event,
	                   @Header(value = KafkaHeaders.RECEIVED_KEY, required = false) String messageKey,
	                   @Header(value = KafkaHeaders.RECEIVED_PARTITION, required = false) Integer partition,
	                   @Header(value = KafkaHeaders.OFFSET, required = false) Integer offset) {
		logReceived(event, messageKey, partition, offset);
//		simulateProcessing();
		productCacheManager.evict(event.getProductId());
		forManagingShoppingCarts.changeProductAvailability(event.getProductId(), true);
	}

	@KafkaHandler
	public void handle(@Payload ProductDelistedIntegrationEvent event,
	                   @Header(value = KafkaHeaders.RECEIVED_KEY, required = false) String messageKey,
	                   @Header(value = KafkaHeaders.RECEIVED_PARTITION, required = false) Integer partition,
	                   @Header(value = KafkaHeaders.OFFSET, required = false) Integer offset) {
		logReceived(event, messageKey, partition, offset);
//		simulateProcessing();
		productCacheManager.evict(event.getProductId());
		forManagingShoppingCarts.changeProductAvailability(event.getProductId(), false);
	}

	@KafkaHandler
	public void handle(@Payload @Valid ProductPriceChangedV2IntegrationEvent event,
	                   @Header(value = KafkaHeaders.RECEIVED_KEY, required = false) String messageKey,
	                   @Header(value = KafkaHeaders.RECEIVED_PARTITION, required = false) Integer partition,
	                   @Header(value = KafkaHeaders.OFFSET, required = false) Integer offset) {
		logReceived(event, messageKey, partition, offset);

		productCacheManager.evict(event.getProductId());
		forManagingShoppingCarts.refreshProductPrice(event.getProductId(), event.getNewSalePrice());

		log.warn("Mail send product price on shopping cart was been updated.");

		simulateProcessing();
	}

	private void simulateProcessing() {
		switch (simulate) {
			case "slow" -> {
				log.warn("Simulate: Slow call");
				try {
					Thread.sleep(Duration.ofSeconds(30));
				} catch (InterruptedException e) {
					Thread.currentThread().interrupt();
				}
			}
			case "technical" -> throw new RuntimeException("Simulated failure: database is down");
			case "business" -> throw new DomainException("Simulated business: rule violation");
		}
	}

	private void logReceived(Object event, String messageKey, Integer partition, Integer offset) {
		log.info("Received {} | key={} | partiton={} | offset={} | thread={}",
				event.getClass().getSimpleName(), messageKey, partition, offset,
				Thread.currentThread().getName());
	}

}
