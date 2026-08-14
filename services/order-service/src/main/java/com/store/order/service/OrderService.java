package com.store.order.service;

import com.store.order.dto.OrderRequest;
import com.store.order.dto.OrderResponse;
import com.store.order.entity.Order;
import com.store.order.entity.OrderItem;
import com.store.order.entity.OrderStatus;
import com.store.order.event.OrderCreatedEvent;
import com.store.order.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class OrderService {

    private final OrderRepository orderRepository;
    private final KafkaTemplate<String, Object> kafkaTemplate;

    private static final String ORDER_TOPIC = "order-events";

    @Transactional
    public OrderResponse placeOrder(OrderRequest request) {
        String orderNumber = "ORD-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();

        BigDecimal totalAmount = request.items().stream()
                .map(item -> item.price().multiply(BigDecimal.valueOf(item.quantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        Order order = Order.builder()
                .orderNumber(orderNumber)
                .customerId(request.customerId())
                .status(OrderStatus.PENDING)
                .totalAmount(totalAmount)
                .build();

        List<OrderItem> items = request.items().stream()
                .map(itemReq -> OrderItem.builder()
                        .sku(itemReq.sku())
                        .quantity(itemReq.quantity())
                        .price(itemReq.price())
                        .order(order)
                        .build())
                .toList();

        order.setItems(items);
        Order savedOrder = orderRepository.save(order);
        log.info("Order {} saved to PostgreSQL database with ID: {}", orderNumber, savedOrder.getId());

        // Publish OrderCreatedEvent to Kafka topic 'order-events'
        List<OrderCreatedEvent.OrderItemEvent> itemEvents = items.stream()
                .map(i -> new OrderCreatedEvent.OrderItemEvent(i.getSku(), i.getQuantity(), i.getPrice()))
                .toList();

        OrderCreatedEvent event = new OrderCreatedEvent(
                savedOrder.getOrderNumber(),
                savedOrder.getCustomerId(),
                savedOrder.getTotalAmount(),
                itemEvents,
                savedOrder.getCreatedAt()
        );

        kafkaTemplate.send(ORDER_TOPIC, savedOrder.getOrderNumber(), event);
        log.info("OrderCreatedEvent published to Kafka topic '{}' with key '{}'", ORDER_TOPIC, savedOrder.getOrderNumber());

        return mapToResponse(savedOrder);
    }

    public List<OrderResponse> getAllOrders() {
        return orderRepository.findAll().stream()
                .map(this::mapToResponse)
                .toList();
    }

    public OrderResponse getOrderByOrderNumber(String orderNumber) {
        return orderRepository.findByOrderNumber(orderNumber)
                .map(this::mapToResponse)
                .orElseThrow(() -> new RuntimeException("Order not found: " + orderNumber));
    }

    private OrderResponse mapToResponse(Order order) {
        List<OrderResponse.OrderItemDto> itemDtos = order.getItems().stream()
                .map(i -> new OrderResponse.OrderItemDto(i.getSku(), i.getQuantity(), i.getPrice()))
                .toList();

        return new OrderResponse(
                order.getId(),
                order.getOrderNumber(),
                order.getCustomerId(),
                order.getStatus(),
                order.getTotalAmount(),
                itemDtos,
                order.getCreatedAt()
        );
    }
}