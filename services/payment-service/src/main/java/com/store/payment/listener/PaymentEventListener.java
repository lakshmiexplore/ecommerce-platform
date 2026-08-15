package com.store.payment.listener;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.store.payment.event.InventoryReservedEvent;
import com.store.payment.service.PaymentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
@Slf4j
@RequiredArgsConstructor
public class PaymentEventListener {

    private final PaymentService paymentService;
    private final ObjectMapper objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());

    @KafkaListener(topics = "inventory-events", groupId = "payment-group")
    public void handleInventoryReserved(String messagePayload) {
        try {
            InventoryReservedEvent event = objectMapper.readValue(messagePayload, InventoryReservedEvent.class);
            log.info("📥 Received InventoryReservedEvent for Order: {} with Status: {}", event.orderNumber(), event.status());

            if ("RESERVED".equalsIgnoreCase(event.status())) {
                paymentService.processPaymentForOrder(event.orderNumber());
            } else {
                log.warn("⚠️ Inventory reservation was not successful (Status: {}). Skipping payment.", event.status());
            }
        } catch (Exception e) {
            log.error("Error processing inventory event in PaymentService: {}", messagePayload, e);
        }
    }
}