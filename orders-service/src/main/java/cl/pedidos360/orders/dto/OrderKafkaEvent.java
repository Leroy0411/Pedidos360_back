package cl.pedidos360.orders.dto;

import java.math.BigDecimal;
import java.time.Instant;

public record OrderKafkaEvent(
        String eventId,
        String eventType,
        Long orderId,
        String customerEmail,
        Long productId,
        Integer quantity,
        BigDecimal totalAmount,
        String status,
        Instant occurredAt
) {}
