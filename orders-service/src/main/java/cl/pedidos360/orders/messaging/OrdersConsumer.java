package cl.pedidos360.orders.messaging;

import com.rabbitmq.client.Channel;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;
import java.io.IOException;

@Component
public class OrdersConsumer {
    private final MessageAcknowledger acknowledger;
    public OrdersConsumer(MessageAcknowledger acknowledger) { this.acknowledger = acknowledger; }

    @RabbitListener(queues = "${pedidos360.messaging.queues.orders}")
    public void onCreated(Message message, Channel channel) throws IOException {
        acknowledger.consume("orders", message, channel);
    }
}
