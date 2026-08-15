package com.store.inventory.listener;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.store.inventory.event.OrderCreatedEvent;
import com.store.inventory.service.InventoryService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class OrderEventListener {

    private final InventoryService inventoryService;
    private final ObjectMapper objectMapper;

    public OrderEventListener(InventoryService inventoryService) {
        this.inventoryService = inventoryService;
        this.objectMapper = new ObjectMapper();
        this.objectMapper.registerModule(new JavaTimeModule());
    }

    @KafkaListener(topics = "order-events", groupId = "inventory-group")
    public void handleOrderCreated(String messagePayload) {
        log.info("Kafka consumer received raw order event: {}", messagePayload);
        try {
            OrderCreatedEvent event = objectMapper.readValue(messagePayload, OrderCreatedEvent.class);
            log.info("Successfully parsed event for order: {}", event.orderNumber());
            inventoryService.processOrderCreated(event);
        } catch (Exception e) {
            log.error("Failed to parse OrderCreatedEvent from message: {}", messagePayload, e);
        }
    }
}