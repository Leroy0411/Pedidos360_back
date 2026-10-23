package cl.pedidos360.orders.messaging;

import com.rabbitmq.client.Channel;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;
import java.io.IOException;

@Component
public class DocumentsConsumer {
    private final MessageAcknowledger acknowledger;
    public DocumentsConsumer(MessageAcknowledger acknowledger) { this.acknowledger = acknowledger; }

    @RabbitListener(queues = "${pedidos360.messaging.queues.documents}")
    public void onDocument(Message message, Channel channel) throws IOException {
        acknowledger.consume("documents", message, channel);
    }
}
