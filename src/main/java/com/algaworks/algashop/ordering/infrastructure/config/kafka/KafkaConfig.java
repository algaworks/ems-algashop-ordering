package com.algaworks.algashop.ordering.infrastructure.config.kafka;

import com.algaworks.algashop.ordering.core.domain.model.DomainException;
import org.apache.kafka.clients.admin.NewTopic;
import org.apache.kafka.common.TopicPartition;
import org.jspecify.annotations.NonNull;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.listener.DeadLetterPublishingRecoverer;
import org.springframework.kafka.listener.DefaultErrorHandler;
import org.springframework.util.backoff.ExponentialBackOff;

import java.time.Duration;
import java.util.Map;

@Configuration
public class KafkaConfig {

	private static final String DLT_PREFIX = "ordering.dlt.";

	@Bean
	public DefaultErrorHandler defaultErrorHandler(DeadLetterPublishingRecoverer recoverer) {
		long interval = 2000L; //2s entre as tentativas
		double multiplier = 2;
		long maxRetries = 3L; //2 retentativas
		ExponentialBackOff exponentialBackOff = new ExponentialBackOff(interval, multiplier);
		exponentialBackOff.setMaxAttempts(maxRetries);
		DefaultErrorHandler defaultErrorHandler = new DefaultErrorHandler(recoverer, exponentialBackOff);
		defaultErrorHandler.addNotRetryableExceptions(DomainException.class);
		return defaultErrorHandler;
	}

	@Bean
	public DeadLetterPublishingRecoverer deadLetterPublishingRecoverer(
			KafkaTemplate<String, Object> kafkaTemplate) {
		DeadLetterPublishingRecoverer recoverer = new DeadLetterPublishingRecoverer(kafkaTemplate,
				(consumerRecord, exception) -> new TopicPartition(
						DLT_PREFIX + consumerRecord.topic(),
						consumerRecord.partition()));
		recoverer.setLogRecoveryRecord(true);
		recoverer.setFailIfSendResultIsError(false);
		return recoverer;
	}

	@Bean
	public NewTopic productEventsDlt(AlgaShopMessagingKafkaProperties properties) {
		return createDeadLetterTopic(properties.getProductEventTopicName());
	}

	@Bean
	public NewTopic orderEventsDlt(AlgaShopMessagingKafkaProperties properties) {
		return createDeadLetterTopic(properties.getOrderEventTopicName());
	}

	private NewTopic createDeadLetterTopic(String originTopicName) {
		return TopicBuilder.name(DLT_PREFIX + originTopicName)
				.partitions(3) //same quantity as the source topic
				.replicas(3)
				.configs(Map.of(
						"min.insync.replicas", "2",
						"retention.ms", String.valueOf(Duration.ofDays(30).toMillis())
				))
				.build();
	}

	@Bean
	public NewTopic ordersEventTopic(AlgaShopMessagingKafkaProperties properties) {
		return TopicBuilder.name(properties.getOrderEventTopicName())
				.partitions(3)
				.replicas(3)
				.configs(Map.of(
						"min.insync.replicas", "2"
				))
				.build();
	}

}
