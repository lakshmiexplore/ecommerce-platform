package com.store.notification.event;

import java.time.LocalDateTime;
import java.util.List;

public record InventoryReservedEvent(
    String orderNumber,
    String status,
    List<String> skus,
    LocalDateTime timestamp
) {}