package cl.pedidos360.products.messaging;

import cl.pedidos360.products.dto.OrderKafkaEvent;
import cl.pedidos360.products.repository.ProductRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.listener.DeadLetterPublishingRecoverer;
import org.springframework.kafka.listener.DefaultErrorHandler;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.backoff.FixedBackOff;

import java.time.Instant;
import java.util.UUID;

@Component
public class InventoryKafkaConsumer {
    private static final Logger log = LoggerFactory.getLogger(InventoryKafkaConsumer.class);

    private final ProductRepository productRepository;
    private final KafkaTemplate<String, OrderKafkaEvent> kafkaTemplate;
    private final String confirmedTopic;

    public InventoryKafkaConsumer(
            ProductRepository productRepository,
            KafkaTemplate<String, OrderKafkaEvent> kafkaTemplate,
            @Value("${pedidos360.kafka.topics.confirmed:pedidos360.inventory.confirmed}") String confirmedTopic) {
        this.productRepository = productRepository;
        this.kafkaTemplate = kafkaTemplate;
        this.confirmedTopic = confirmedTopic;
    }

    @Bean
    public DefaultErrorHandler inventoryErrorHandler(KafkaTemplate<Object, Object> template) {
        DeadLetterPublishingRecoverer recoverer = new DeadLetterPublishingRecoverer(template);
        DefaultErrorHandler handler = new DefaultErrorHandler(recoverer, new FixedBackOff(1000L, 2));
        handler.setRetryListeners((record, ex, deliveryAttempt) ->
                log.warn("[Kafka-Products] Reintento {} en offset {} por error: {}", deliveryAttempt, record.offset(), ex.getMessage()));
        return handler;
    }

    @Transactional
    @KafkaListener(
            topics = "${pedidos360.kafka.topics.inventory:pedidos360.inventory.events}",
            groupId = "products-inventory-group"
    )
    public void onOrderCreated(OrderKafkaEvent event) {
        log.info("[Kafka Consumer - products-inventory-group] Procesando descuento de stock: orderId={}, productId={}, qty={}",
                event.orderId(), event.productId(), event.quantity());

        productRepository.findById(event.productId()).ifPresentOrElse(product -> {
            int newStock = Math.max(0, product.getStock() - event.quantity());
            product.setStock(newStock);
            productRepository.save(product);
            log.info("[Inventario Actualizado] SKU={} ({}) stock nuevo={}", product.getSku(), product.getName(), newStock);

            OrderKafkaEvent confirmed = new OrderKafkaEvent(
                    UUID.randomUUID().toString(),
                    "STOCK_RESERVED",
                    event.orderId(),
                    event.customerEmail(),
                    event.productId(),
                    event.quantity(),
                    event.totalAmount(),
                    "CONFIRMED",
                    Instant.now()
            );
            kafkaTemplate.send(confirmedTopic, String.valueOf(event.orderId()), confirmed);
        }, () -> log.warn("[Kafka Consumer] Producto productId={} no encontrado para orderId={}", event.productId(), event.orderId()));
    }
}
