package com.store.inventory.dto;

public record InventoryResponse(
    Long id,
    String sku,
    Integer quantity,
    Integer reservedQuantity,
    boolean inStock
) {}