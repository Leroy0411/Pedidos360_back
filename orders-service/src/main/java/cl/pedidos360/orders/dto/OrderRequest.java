package cl.pedidos360.orders.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public record OrderRequest(
        @NotBlank @Email String customerEmail,
        @NotNull Long productId,
        @NotNull @Min(1) Integer quantity,
        @NotNull BigDecimal totalAmount
) {
}
