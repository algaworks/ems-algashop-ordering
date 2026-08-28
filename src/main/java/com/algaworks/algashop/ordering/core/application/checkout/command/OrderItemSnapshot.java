package com.algaworks.algashop.ordering.core.application.checkout.command;

import java.math.BigDecimal;
import java.util.UUID;

public record OrderItemSnapshot(
            String orderItemId,
            UUID productId,
            String productName,
            BigDecimal price,
            Integer quantity,
            BigDecimal totalAmount
) {
}