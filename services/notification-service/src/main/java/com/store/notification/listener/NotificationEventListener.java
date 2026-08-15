package com.store.notification.listener;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.store.notification.event.InventoryReservedEvent;
import com.store.notification.event.OrderCreatedEvent;
import com.store.notification.service.NotificationService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class NotificationEventListener {

    private final NotificationService notificationService;
    private final ObjectMapper objectMapper;

    public NotificationEventListener(NotificationService notificationService) {
        this.notificationService = notificationService;
        this.objectMapper = new ObjectMapper();
        this.objectMapper.registerModule(new JavaTimeModule());
    }

    @KafkaListener(topics = "order-events", groupId = "notification-group")
    public void handleOrderEvents(String messagePayload) {
        try {
            OrderCreatedEvent event = objectMapper.readValue(messagePayload, OrderCreatedEvent.class);
            notificationService.sendOrderConfirmation(event);
        } catch (Exception e) {
            log.error("Error deserializing OrderCreatedEvent payload: {}", messagePayload, e);
        }
    }

    @KafkaListener(topics = "inventory-events", groupId = "notification-group")
    public void handleInventoryEvents(String messagePayload) {
        try {
            InventoryReservedEvent event = objectMapper.readValue(messagePayload, InventoryReservedEvent.class);
            notificationService.sendInventoryStatusUpdate(event);
        } catch (Exception e) {
            log.error("Error deserializing InventoryReservedEvent payload: {}", messagePayload, e);
        }
    }
}