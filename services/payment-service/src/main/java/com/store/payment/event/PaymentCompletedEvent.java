package com.store.payment.event;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record PaymentCompletedEvent(
    String transactionId,
    String orderNumber,
    BigDecimal amount,
    String status,
    LocalDateTime completedAt
) {}