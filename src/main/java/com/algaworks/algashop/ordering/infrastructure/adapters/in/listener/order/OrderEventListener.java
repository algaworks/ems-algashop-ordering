package com.algaworks.algashop.ordering.infrastructure.adapters.in.listener.order;

import com.algaworks.algashop.ordering.core.application.order.event.OrderPlacedIntegrationEvent;
import com.algaworks.algashop.ordering.core.application.utility.Mapper;
import com.algaworks.algashop.ordering.core.domain.model.order.OrderCanceledEvent;
import com.algaworks.algashop.ordering.core.domain.model.order.OrderPaidEvent;
import com.algaworks.algashop.ordering.core.domain.model.order.OrderPlacedEvent;
import com.algaworks.algashop.ordering.core.domain.model.order.OrderReadyEvent;
import com.algaworks.algashop.ordering.core.ports.out.order.ForPublishingOrderIntegrationEvents;
import lombok.RequiredArgsConstructor;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class OrderEventListener {

    private final ForPublishingOrderIntegrationEvents forPublishingOrderIntegrationEvents;
    private final Mapper mapper;

    @EventListener
    public void listen(OrderPlacedEvent event) {
        OrderPlacedIntegrationEvent integrationEvent = mapper.convert(event, OrderPlacedIntegrationEvent.class);
        forPublishingOrderIntegrationEvents.send(integrationEvent);
    }

    @EventListener
    public void listen(OrderPaidEvent event) {

    }

    @EventListener
    public void listen(OrderReadyEvent event) {

    }

    @EventListener
    public void listen(OrderCanceledEvent event) {

    }

}
