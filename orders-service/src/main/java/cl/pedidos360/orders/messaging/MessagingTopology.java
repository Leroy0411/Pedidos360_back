package cl.pedidos360.orders.messaging;

import org.springframework.amqp.core.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/** All messaging declarations live here; application services never declare broker resources. */
@Configuration
public class MessagingTopology {
    @Bean TopicExchange eventsExchange(@Value("${pedidos360.messaging.exchanges.events}") String name) {
        return ExchangeBuilder.topicExchange(name).durable(true).build();
    }

    @Bean DirectExchange commandsExchange(@Value("${pedidos360.messaging.exchanges.commands}") String name) {
        return ExchangeBuilder.directExchange(name).durable(true).build();
    }

    @Bean DirectExchange deadExchange(@Value("${pedidos360.messaging.exchanges.dead}") String name) {
        return ExchangeBuilder.directExchange(name).durable(true).build();
    }

    private Queue mainQueue(String name, String deadExchange, String deadRouting) {
        return QueueBuilder.durable(name).deadLetterExchange(deadExchange).deadLetterRoutingKey(deadRouting).build();
    }

    @Bean Queue ordersQueue(@Value("${pedidos360.messaging.queues.orders}") String name,
                            @Value("${pedidos360.messaging.exchanges.dead}") String dead,
                            @Value("${pedidos360.messaging.routing.orders-dead}") String key) {
        return mainQueue(name, dead, key);
    }

    @Bean Queue notificationsQueue(@Value("${pedidos360.messaging.queues.notifications}") String name,
                                   @Value("${pedidos360.messaging.exchanges.dead}") String dead,
                                   @Value("${pedidos360.messaging.routing.notifications-dead}") String key) {
        return mainQueue(name, dead, key);
    }

    @Bean Queue documentsQueue(@Value("${pedidos360.messaging.queues.documents}") String name,
                               @Value("${pedidos360.messaging.exchanges.dead}") String dead,
                               @Value("${pedidos360.messaging.routing.documents-dead}") String key) {
        return mainQueue(name, dead, key);
    }

    @Bean Queue ordersDlq(@Value("${pedidos360.messaging.queues.orders-dlq}") String name) {
        return QueueBuilder.durable(name).build();
    }

    @Bean Queue notificationsDlq(@Value("${pedidos360.messaging.queues.notifications-dlq}") String name) {
        return QueueBuilder.durable(name).build();
    }

    @Bean Queue documentsDlq(@Value("${pedidos360.messaging.queues.documents-dlq}") String name) {
        return QueueBuilder.durable(name).build();
    }

    @Bean Binding ordersBinding(Queue ordersQueue, TopicExchange eventsExchange,
                                @Value("${pedidos360.messaging.routing.orders}") String key) {
        return BindingBuilder.bind(ordersQueue).to(eventsExchange).with(key);
    }

    @Bean Binding notificationsBinding(Queue notificationsQueue, TopicExchange eventsExchange,
                                       @Value("${pedidos360.messaging.routing.notifications}") String key) {
        return BindingBuilder.bind(notificationsQueue).to(eventsExchange).with(key);
    }

    @Bean Binding documentsBinding(Queue documentsQueue, DirectExchange commandsExchange,
                                   @Value("${pedidos360.messaging.routing.documents}") String key) {
        return BindingBuilder.bind(documentsQueue).to(commandsExchange).with(key);
    }

    @Bean Binding ordersDeadBinding(Queue ordersDlq, DirectExchange deadExchange,
                                    @Value("${pedidos360.messaging.routing.orders-dead}") String key) {
        return BindingBuilder.bind(ordersDlq).to(deadExchange).with(key);
    }

    @Bean Binding notificationsDeadBinding(Queue notificationsDlq, DirectExchange deadExchange,
                                           @Value("${pedidos360.messaging.routing.notifications-dead}") String key) {
        return BindingBuilder.bind(notificationsDlq).to(deadExchange).with(key);
    }

    @Bean Binding documentsDeadBinding(Queue documentsDlq, DirectExchange deadExchange,
                                       @Value("${pedidos360.messaging.routing.documents-dead}") String key) {
        return BindingBuilder.bind(documentsDlq).to(deadExchange).with(key);
    }
}
