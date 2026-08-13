package com.store.customer.dto;

import java.time.LocalDateTime;

public record CustomerResponse(
    Long id,
    String email,
    String firstName,
    String lastName,
    String phone,
    String address,
    LocalDateTime createdAt
) {}