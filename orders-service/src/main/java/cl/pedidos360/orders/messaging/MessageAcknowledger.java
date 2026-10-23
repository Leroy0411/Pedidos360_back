package cl.pedidos360.orders.messaging;

import com.rabbitmq.client.Channel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.Message;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

/** Manual ACK after successful work; permanent failures route to the queue's DLX. */
@Component
public class MessageAcknowledger {
    private static final Logger log = LoggerFactory.getLogger(MessageAcknowledger.class);

    public void consume(String domain, Message message, Channel channel) throws IOException {
        long tag = message.getMessageProperties().getDeliveryTag();
        String payload = new String(message.getBody(), StandardCharsets.UTF_8);
        try {
            // Require a stable event reference. An intentionally malformed message can test the DLQ.
            if (!payload.matches("orderId=[0-9]+;customer=.+")) {
                throw new IllegalArgumentException("Invalid order message format");
            }
            // Demonstration-only business operation. Real integrations can replace this handler.
            log.info("Processed domain={} payload={}", domain, payload.replaceAll("customer=.*", "customer=[redacted]"));
            channel.basicAck(tag, false);
        } catch (IllegalArgumentException permanent) {
            log.error("Rejected malformed message domain={} deliveryTag={} to DLQ: {}", domain, tag, permanent.getMessage());
            channel.basicNack(tag, false, false);
        } catch (RuntimeException transientFailure) {
            // Avoid infinite requeue loops. Unexpected errors are dead-lettered for investigation.
            log.error("Failed to process domain={} deliveryTag={} to DLQ", domain, tag, transientFailure);
            channel.basicNack(tag, false, false);
        }
    }
}
