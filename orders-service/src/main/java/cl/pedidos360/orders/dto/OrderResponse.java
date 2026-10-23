package cl.pedidos360.orders.dto;

import cl.pedidos360.orders.entity.Order;
import cl.pedidos360.orders.entity.OrderStatus;

import java.math.BigDecimal;
import java.time.Instant;

public record OrderResponse(
        Long id,
        String customerEmail,
        Long productId,
        Integer quantity,
        BigDecimal totalAmount,
        OrderStatus status,
        Instant createdAt
) {

    public static OrderResponse from(Order order) {
        return new OrderResponse(
                order.getId(),
                order.getCustomerEmail(),
                order.getProductId(),
                order.getQuantity(),
                order.getTotalAmount(),
                order.getStatus(),
                order.getCreatedAt()
        );
    }
}
