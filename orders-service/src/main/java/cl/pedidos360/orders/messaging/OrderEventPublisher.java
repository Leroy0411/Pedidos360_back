package cl.pedidos360.orders.messaging;

import cl.pedidos360.orders.dto.OrderResponse;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class OrderEventPublisher {
    private final RabbitTemplate template;
    private final OrderKafkaMessaging kafkaMessaging;
    private final String eventsExchange;
    private final String commandsExchange;
    private final String ordersKey;
    private final String notificationsKey;
    private final String documentsKey;

    public OrderEventPublisher(RabbitTemplate template,
            OrderKafkaMessaging kafkaMessaging,
            @Value("${pedidos360.messaging.exchanges.events}") String eventsExchange,
            @Value("${pedidos360.messaging.exchanges.commands}") String commandsExchange,
            @Value("${pedidos360.messaging.routing.orders}") String ordersKey,
            @Value("${pedidos360.messaging.routing.notifications}") String notificationsKey,
            @Value("${pedidos360.messaging.routing.documents}") String documentsKey) {
        this.template = template;
        this.kafkaMessaging = kafkaMessaging;
        this.eventsExchange = eventsExchange;
        this.commandsExchange = commandsExchange;
        this.ordersKey = ordersKey;
        this.notificationsKey = notificationsKey;
        this.documentsKey = documentsKey;
    }

    public void orderCreated(OrderResponse order) {
        String payload = "orderId=" + order.id() + ";customer=" + order.customerEmail();
        template.convertAndSend(eventsExchange, ordersKey, payload);
        template.convertAndSend(eventsExchange, notificationsKey, payload);
        template.convertAndSend(commandsExchange, documentsKey, payload);
        kafkaMessaging.publishOrderCreated(order);
    }
}
