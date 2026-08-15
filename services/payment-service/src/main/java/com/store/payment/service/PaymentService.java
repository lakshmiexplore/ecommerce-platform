package com.store.payment.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.store.payment.entity.Payment;
import com.store.payment.event.PaymentCompletedEvent;
import com.store.payment.repository.PaymentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class PaymentService {

    private final PaymentRepository paymentRepository;
    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ObjectMapper objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());

    private static final String PAYMENT_TOPIC = "payment-events";

    @Transactional
    public void processPaymentForOrder(String orderNumber) {
        log.info("💳 Processing simulated payment for Order: {}", orderNumber);

        // Check idempotency (prevent double charge)
        if (paymentRepository.findByOrderNumber(orderNumber).isPresent()) {
            log.warn("Payment already processed for order: {}", orderNumber);
            return;
        }

        // Simulate successful charge of $199.99 (or dynamic)
        String txnId = "TXN-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        Payment payment = Payment.builder()
                .transactionId(txnId)
                .orderNumber(orderNumber)
                .amount(new BigDecimal("199.99"))
                .status("SUCCESS")
                .paymentMethod("CREDIT_CARD")
                .createdAt(LocalDateTime.now())
                .build();

        Payment savedPayment = paymentRepository.save(payment);
        log.info("✅ Payment successful! Txn ID: {}, Order: {}", txnId, orderNumber);

        // Emit PaymentCompletedEvent to Kafka
        PaymentCompletedEvent event = new PaymentCompletedEvent(
                savedPayment.getTransactionId(),
                savedPayment.getOrderNumber(),
                savedPayment.getAmount(),
                savedPayment.getStatus(),
                savedPayment.getCreatedAt()
        );

        try {
            String payload = objectMapper.writeValueAsString(event);
            kafkaTemplate.send(PAYMENT_TOPIC, event.orderNumber(), payload);
            log.info("🚀 Emitted PaymentCompletedEvent to topic '{}' for Order: {}", PAYMENT_TOPIC, orderNumber);
        } catch (Exception e) {
            log.error("Failed to publish PaymentCompletedEvent for order: {}", orderNumber, e);
        }
    }

    public List<Payment> getAllPayments() {
        return paymentRepository.findAll();
    }
}