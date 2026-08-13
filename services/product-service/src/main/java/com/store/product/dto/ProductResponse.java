package com.store.product.dto;

import java.math.BigDecimal;
import java.util.Map;

public record ProductResponse(
    String id,
    String sku,
    String name,
    String description,
    BigDecimal price,
    String category,
    boolean inStock,
    Map<String, String> attributes
) {}