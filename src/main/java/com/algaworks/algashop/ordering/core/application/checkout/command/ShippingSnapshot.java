package com.algaworks.algashop.ordering.core.application.checkout.command;

import java.math.BigDecimal;
import java.time.LocalDate;

public record ShippingSnapshot(
            BigDecimal cost,
            LocalDate expectedDate,
            RecipientSnapshot recipient,
            AddressSnapshot address
    ) {
    }