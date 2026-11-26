package cl.pedidos360.orders.messaging;

import cl.pedidos360.orders.dto.OrderKafkaEvent;
import cl.pedidos360.orders.dto.OrderResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CopyOnWriteArrayList;

@Component
public class OrderKafkaMessaging {
    private static final Logger log = LoggerFactory.getLogger(OrderKafkaMessaging.class);

    private final KafkaTemplate<String, OrderKafkaEvent> kafkaTemplate;
    private final String ordersTopic;
    private final String inventoryTopic;
    private final List<OrderKafkaEvent> recentAuditEvents = new CopyOnWriteArrayList<>();

    public OrderKafkaMessaging(
            KafkaTemplate<String, OrderKafkaEvent> kafkaTemplate,
            @Value("${pedidos360.kafka.topics.orders:pedidos360.orders.events}") String ordersTopic,
            @Value("${pedidos360.kafka.topics.inventory:pedidos360.inventory.events}") String inventoryTopic) {
        this.kafkaTemplate = kafkaTemplate;
        this.ordersTopic = ordersTopic;
        this.inventoryTopic = inventoryTopic;
    }

    public void publishOrderCreated(OrderResponse order) {
        OrderKafkaEvent event = new OrderKafkaEvent(
                UUID.randomUUID().toString(),
                "ORDER_CREATED",
                order.id(),
                order.customerEmail(),
                order.productId(),
                order.quantity(),
                order.totalAmount(),
                order.status().name(),
                Instant.now()
        );
        try {
            kafkaTemplate.send(ordersTopic, String.valueOf(order.id()), event);
            kafkaTemplate.send(inventoryTopic, String.valueOf(order.productId()), event);
            log.info("[Kafka Producer] Evento ORDER_CREATED publicado en {} y {} para orderId={}", ordersTopic, inventoryTopic, order.id());
        } catch (Exception ex) {
            log.error("[Kafka Producer] Error publicando evento Kafka para orderId={}: {}", order.id(), ex.getMessage());
        }
    }

    @KafkaListener(
            topics = "${pedidos360.kafka.topics.orders:pedidos360.orders.events}",
            groupId = "orders-audit-group"
    )
    public void consumeOrderAudit(OrderKafkaEvent event) {
        log.info("[Kafka Consumer - orders-audit-group] Evento recibido: eventId={}, orderId={}, customer={}, total={}",
                event.eventId(), event.orderId(), event.customerEmail(), event.totalAmount());
        recentAuditEvents.add(0, event);
        if (recentAuditEvents.size() > 50) {
            recentAuditEvents.remove(recentAuditEvents.size() - 1);
        }
    }

    public List<OrderKafkaEvent> getRecentAuditEvents() {
        return List.copyOf(recentAuditEvents);
    }
}
