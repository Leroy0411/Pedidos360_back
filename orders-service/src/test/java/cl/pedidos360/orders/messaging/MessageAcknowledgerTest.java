package cl.pedidos360.orders.messaging;

import com.rabbitmq.client.Channel;
import org.junit.jupiter.api.Test;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageProperties;

import java.nio.charset.StandardCharsets;

import static org.mockito.Mockito.*;

class MessageAcknowledgerTest {
    private final MessageAcknowledger subject = new MessageAcknowledger();

    private Message message(String body) {
        MessageProperties props = new MessageProperties();
        props.setDeliveryTag(17L);
        return new Message(body.getBytes(StandardCharsets.UTF_8), props);
    }

    @Test void validMessageIsAcknowledgedExactlyOnce() throws Exception {
        Channel channel = mock(Channel.class);
        subject.consume("orders", message("orderId=42;customer=test@example.cl"), channel);
        verify(channel).basicAck(17L, false);
        verify(channel, never()).basicNack(anyLong(), anyBoolean(), anyBoolean());
    }

    @Test void invalidMessageIsRejectedWithoutRequeueForDlq() throws Exception {
        Channel channel = mock(Channel.class);
        subject.consume("orders", message("bad-event"), channel);
        verify(channel).basicNack(17L, false, false);
        verify(channel, never()).basicAck(anyLong(), anyBoolean());
    }
}
