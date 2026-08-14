package com.store.order.dto;

import com.store.order.entity.OrderStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public record OrderResponse(
    Long id,
    String orderNumber,
    Long customerId,
    OrderStatus status,
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