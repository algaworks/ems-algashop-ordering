package com.algaworks.algashop.ordering.core.application.order.event;

import java.math.BigDecimal;
import java.time.LocalDate;

public record ShippingSnapshot(
            BigDecimal cost,
            LocalDate expectedDate,
            RecipientSnapshot recipient,
            AddressSnapshot address
    ) {
    }