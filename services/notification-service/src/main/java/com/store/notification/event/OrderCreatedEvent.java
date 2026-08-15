package com.store.notification.event;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public record OrderCreatedEvent(
    String orderNumber,
    Long customerId,
    BigDecimal totalAmount,
    List<OrderItemDto> items,
    LocalDateTime createdAt
) {
    public record OrderItemDto(
        String sku,
        Integer quantity,
        BigDecimal price
    ) {}
}