package cl.pedidos360.orders.service;

import cl.pedidos360.orders.dto.OrderRequest;
import cl.pedidos360.orders.dto.OrderResponse;
import cl.pedidos360.orders.entity.Order;
import cl.pedidos360.orders.entity.OrderStatus;
import cl.pedidos360.orders.repository.OrderRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
public class OrderService {

    private final OrderRepository orderRepository;

    public OrderService(OrderRepository orderRepository) {
        this.orderRepository = orderRepository;
    }

    public List<OrderResponse> findAll() {
        return orderRepository.findAll().stream()
                .map(OrderResponse::from)
                .toList();
    }

    public OrderResponse findById(Long id) {
        return orderRepository.findById(id)
                .map(OrderResponse::from)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Pedido no encontrado"));
    }

    public List<OrderResponse> findByCustomerEmail(String email) {
        return orderRepository.findByCustomerEmailOrderByCreatedAtDesc(email).stream()
                .map(OrderResponse::from)
                .toList();
    }

    public OrderResponse create(OrderRequest request) {
        Order order = new Order();
        order.setCustomerEmail(request.customerEmail());
        order.setProductId(request.productId());
        order.setQuantity(request.quantity());
        order.setTotalAmount(request.totalAmount());
        order.setStatus(OrderStatus.PENDING);

        return OrderResponse.from(orderRepository.save(order));
    }

    public OrderResponse confirm(Long id) {
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Pedido no encontrado"));
        order.setStatus(OrderStatus.CONFIRMED);
        return OrderResponse.from(orderRepository.save(order));
    }
}
