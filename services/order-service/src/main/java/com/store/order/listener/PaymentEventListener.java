package com.store.order.listener;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.store.order.entity.OrderStatus;
import com.store.order.event.PaymentCompletedEvent;
import com.store.order.service.OrderService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
@Slf4j
@RequiredArgsConstructor
public class PaymentEventListener {

    private final OrderService orderService;
    private final ObjectMapper objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());

    @KafkaListener(topics = "payment-events", groupId = "order-payment-group")
    public void handlePaymentCompleted(String messagePayload) {
        try {
            PaymentCompletedEvent event = objectMapper.readValue(messagePayload, PaymentCompletedEvent.class);
            log.info("Received PaymentCompletedEvent for Order: {} with Status: {}", event.orderNumber(), event.status());

            if ("SUCCESS".equalsIgnoreCase(event.status())) {
                orderService.updateOrderStatus(event.orderNumber(), OrderStatus.CONFIRMED);
            } else {
                orderService.updateOrderStatus(event.orderNumber(), OrderStatus.CANCELLED);
            }
        } catch (Exception e) {
            log.error("Error deserializing PaymentCompletedEvent in OrderService: {}", messagePayload, e);
        }
    }
}