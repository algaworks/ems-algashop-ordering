package com.algaworks.algashop.ordering.infrastructure.adapters.in.listener.order;

import com.algaworks.algashop.ordering.core.application.order.event.OrderPlacedIntegrationEvent;
import com.algaworks.algashop.ordering.core.application.order.event.OrderPlacedIntegrationEventAssembler;
import com.algaworks.algashop.ordering.core.application.utility.Mapper;
import com.algaworks.algashop.ordering.core.domain.model.order.*;
import com.algaworks.algashop.ordering.core.ports.out.order.ForPublishingOrderIntegrationEvents;
import lombok.RequiredArgsConstructor;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class OrderEventListener {

    private final ForPublishingOrderIntegrationEvents forPublishingOrderIntegrationEvents;
    private final Mapper mapper;

    private final OrderPlacedIntegrationEventAssembler orderPlacedIntegrationEventAssembler;
    private final Orders orders;

    @EventListener
    public void listen(OrderPlacedEvent event) {
        Order order = orders.ofId(event.orderId()).orElseThrow(OrderNotFoundException::new);
        OrderPlacedIntegrationEvent integrationEvent = orderPlacedIntegrationEventAssembler.toIntegrationEvent(order);
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
