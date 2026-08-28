package com.algaworks.algashop.ordering.core.application;

import com.fasterxml.jackson.annotation.JsonIgnore;

public interface IntegrationCommand {
	@JsonIgnore
	String getAggregateId();
}
