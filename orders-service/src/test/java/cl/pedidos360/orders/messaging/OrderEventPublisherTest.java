package cl.pedidos360.orders.messaging;

import cl.pedidos360.orders.dto.OrderResponse;
import cl.pedidos360.orders.entity.OrderStatus;
import org.junit.jupiter.api.Test;
import org.springframework.amqp.rabbit.core.RabbitTemplate;

import java.math.BigDecimal;
import java.time.Instant;

import static org.mockito.ArgumentMatchers.contains;
import static org.mockito.Mockito.*;

class OrderEventPublisherTest {
    @Test void publishesToTopicAndDirectExchanges() {
        RabbitTemplate rabbit = mock(RabbitTemplate.class);
        OrderEventPublisher publisher = new OrderEventPublisher(rabbit,
                "events", "commands", "orders.created", "notifications.send", "documents.generate");
        OrderResponse order = new OrderResponse(23L, "test@example.cl", 1L, 2,
                BigDecimal.TEN, OrderStatus.PENDING, Instant.now());

        publisher.orderCreated(order);

        verify(rabbit).convertAndSend(eq("events"), eq("orders.created"), contains("orderId=23"));
        verify(rabbit).convertAndSend(eq("events"), eq("notifications.send"), contains("orderId=23"));
        verify(rabbit).convertAndSend(eq("commands"), eq("documents.generate"), contains("orderId=23"));
        verifyNoMoreInteractions(rabbit);
    }
}
