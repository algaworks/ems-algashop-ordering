package com.algaworks.algashop.ordering.infrastructure.adapters.out.messaging.outbox;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.support.serializer.JacksonJsonSerializer;

import java.util.Map;

@Configuration
public class OutboxConfig {

	@Bean
	@ConditionalOnProperty(name = "algashop.messaging.outbox.enabled", havingValue = "true")
	public JacksonJsonSerializer<Object> outboxJsonSerializer(
			@Value("${spring.kafka.properties.spring.json.type.mapping:}") String typeMappings
	) {
		JacksonJsonSerializer<Object> serializer = new JacksonJsonSerializer<>();
		serializer.configure(Map.of(JacksonJsonSerializer.TYPE_MAPPINGS, typeMappings), false);
		return serializer;
	}

}
