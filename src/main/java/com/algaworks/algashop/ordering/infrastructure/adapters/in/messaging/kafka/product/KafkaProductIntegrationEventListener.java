package com.algaworks.algashop.ordering.infrastructure.adapters.in.messaging.kafka.product;

import com.algaworks.algashop.ordering.core.application.product.event.ProductDelistedIntegrationEvent;
import com.algaworks.algashop.ordering.core.application.product.event.ProductListedIntegrationEvent;
import com.algaworks.algashop.ordering.core.application.product.event.ProductPriceChangedIntegrationEvent;
import com.algaworks.algashop.ordering.core.ports.in.shoppingcart.ForManagingShoppingCarts;
import com.algaworks.algashop.ordering.infrastructure.config.cache.ProductCacheManager;
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
@KafkaListener(topics = {"#{algaShopMessagingKafkaProperties.productEventTopicName}"})
@RequiredArgsConstructor
public class KafkaProductIntegrationEventListener {

	private final ForManagingShoppingCarts forManagingShoppingCarts;
	private final ProductCacheManager productCacheManager;

	@KafkaHandler(isDefault = true)
	public void handle(@Payload Object event,
	                   @Header(value = KafkaHeaders.RECEIVED_KEY) String messageKey,
	                   @Header(value = KafkaHeaders.OFFSET) String messageOffset
	                   ) {
		log.info("Event ignored: key={} offset={}", messageKey, messageOffset);
	}

	@KafkaHandler
	public void handle(@Payload ProductListedIntegrationEvent event,
			@Header(value = KafkaHeaders.RECEIVED_KEY) String messageKey) {
		log.info("Received " + event.getClass());
		log.info("MessageKey " + messageKey);
		productCacheManager.evict(event.getProductId());
		forManagingShoppingCarts.changeProductAvailability(event.getProductId(), true);
	}

	@KafkaHandler
	public void handle(@Payload ProductDelistedIntegrationEvent event,
			@Header(value = KafkaHeaders.RECEIVED_KEY) String messageKey) {
		log.info("Received " + event.getClass());
		log.info("MessageKey " + messageKey);
		productCacheManager.evict(event.getProductId());
		forManagingShoppingCarts.changeProductAvailability(event.getProductId(), false);
	}

	@KafkaHandler
	public void handle(@Payload ProductPriceChangedIntegrationEvent event,
			@Header(value = KafkaHeaders.RECEIVED_KEY) String messageKey) {
		log.info("Received " + event.getClass());
		log.info("MessageKey " + messageKey);
		productCacheManager.evict(event.getProductId());
		forManagingShoppingCarts.refreshProductPrice(event.getProductId());
	}

}
