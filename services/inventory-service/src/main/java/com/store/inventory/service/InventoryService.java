package com.store.inventory.service;

import com.store.inventory.dto.InventoryResponse;
import com.store.inventory.entity.Inventory;
import com.store.inventory.event.InventoryReservedEvent;
import com.store.inventory.event.OrderCreatedEvent;
import com.store.inventory.repository.InventoryRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class InventoryService {

    private final InventoryRepository inventoryRepository;
    private final KafkaTemplate<String, Object> kafkaTemplate;

    private static final String INVENTORY_EVENTS_TOPIC = "inventory-events";

    @Transactional
    public void processOrderCreated(OrderCreatedEvent event) {
        log.info("Received OrderCreatedEvent for order: {}", event.orderNumber());

        List<String> reservedSkus = new ArrayList<>();
        boolean allInStock = true;

        for (OrderCreatedEvent.OrderItemEvent item : event.items()) {
            Inventory inventory = inventoryRepository.findBySku(item.sku())
                    .orElse(null);

            if (inventory != null && inventory.getQuantity() >= item.quantity()) {
                inventory.setQuantity(inventory.getQuantity() - item.quantity());
                inventory.setReservedQuantity(inventory.getReservedQuantity() + item.quantity());
                inventoryRepository.save(inventory);
                reservedSkus.add(item.sku());
                log.info("Reserved {} units of SKU {}", item.quantity(), item.sku());
            } else {
                allInStock = false;
                log.warn("SKU {} out of stock or insufficient quantity for order {}", item.sku(), event.orderNumber());
            }
        }

        String status = allInStock ? "RESERVED" : "OUT_OF_STOCK";
        InventoryReservedEvent inventoryEvent = new InventoryReservedEvent(
                event.orderNumber(),
                status,
                reservedSkus,
                LocalDateTime.now()
        );

        kafkaTemplate.send(INVENTORY_EVENTS_TOPIC, event.orderNumber(), inventoryEvent);
        log.info("Published InventoryReservedEvent ({}) for order: {}", status, event.orderNumber());
    }

    public List<InventoryResponse> getAllInventory() {
        return inventoryRepository.findAll().stream()
                .map(inv -> new InventoryResponse(
                        inv.getId(),
                        inv.getSku(),
                        inv.getQuantity(),
                        inv.getReservedQuantity(),
                        inv.getQuantity() > 0
                ))
                .toList();
    }
}