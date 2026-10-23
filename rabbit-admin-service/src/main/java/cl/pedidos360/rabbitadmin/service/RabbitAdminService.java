package cl.pedidos360.rabbitadmin.service;

import cl.pedidos360.rabbitadmin.api.AdminRequests.*;
import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.core.RabbitAdmin;
import org.springframework.stereotype.Service;

@Service
public class RabbitAdminService {
    private final RabbitAdmin admin;

    public RabbitAdminService(org.springframework.amqp.rabbit.connection.ConnectionFactory connectionFactory) {
        this.admin = new RabbitAdmin(connectionFactory);
    }

    public void createQueue(QueueRequest request) {
        admin.declareQueue(QueueBuilder.durable(request.name()).build());
    }

    public boolean deleteQueue(String name) { return admin.deleteQueue(name); }

    public void createExchange(ExchangeRequest request) {
        Exchange exchange = switch (request.type()) {
            case "topic" -> ExchangeBuilder.topicExchange(request.name()).durable(true).build();
            case "direct" -> ExchangeBuilder.directExchange(request.name()).durable(true).build();
            default -> throw new IllegalArgumentException("type must be direct or topic");
        };
        admin.declareExchange(exchange);
    }

    public boolean deleteExchange(String name) { return admin.deleteExchange(name); }

    public void createBinding(BindingRequest request) {
        admin.declareBinding(new Binding(request.queue(), Binding.DestinationType.QUEUE,
                request.exchange(), request.routingKey(), null));
    }

    public void deleteBinding(BindingRequest request) {
        admin.removeBinding(new Binding(request.queue(), Binding.DestinationType.QUEUE,
                request.exchange(), request.routingKey(), null));
    }
}
