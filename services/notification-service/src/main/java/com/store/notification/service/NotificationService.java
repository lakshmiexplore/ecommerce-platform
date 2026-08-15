package com.store.notification.service;

import com.store.notification.event.InventoryReservedEvent;
import com.store.notification.event.OrderCreatedEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class NotificationService {

    public void sendOrderConfirmation(OrderCreatedEvent event) {
        log.info("==================================================================");
        log.info("📧 [EMAIL NOTIFICATION] ORDER RECEIVED");
        log.info("To: Customer #{}", event.customerId());
        log.info("Subject: Order Confirmation - {}", event.orderNumber());
        log.info("Total Amount: ${}", event.totalAmount());
        log.info("Items Count: {}", event.items() != null ? event.items().size() : 0);
        log.info("==================================================================");
    }

    public void sendInventoryStatusUpdate(InventoryReservedEvent event) {
        log.info("==================================================================");
        log.info("📱 [SMS / EMAIL NOTIFICATION] INVENTORY UPDATE");
        log.info("Order: {}", event.orderNumber());
        log.info("Status: {}", event.status());
        log.info("SKUs Processed: {}", event.skus());
        log.info("Timestamp: {}", event.timestamp());
        log.info("==================================================================");
    }
}