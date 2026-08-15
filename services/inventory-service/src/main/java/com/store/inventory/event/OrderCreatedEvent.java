package com.store.inventory.event;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public record OrderCreatedEvent(
    String orderNumber,
    Long customerId,
    BigDecimal totalAmount,
    List<OrderItemEvent> items,
    LocalDateTime createdAt
) {
    public record OrderItemEvent(
        String sku,
        Integer quantity,
        BigDecimal price
    ) {}
}