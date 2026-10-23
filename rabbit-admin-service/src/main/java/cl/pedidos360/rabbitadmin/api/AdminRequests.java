package cl.pedidos360.rabbitadmin.api;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public final class AdminRequests {
    private AdminRequests() {}
    public record QueueRequest(@NotBlank @Pattern(regexp="[a-zA-Z0-9._-]{1,120}") String name) {}
    public record ExchangeRequest(@NotBlank @Pattern(regexp="[a-zA-Z0-9._-]{1,120}") String name,
                                  @NotBlank @Pattern(regexp="direct|topic") String type) {}
    public record BindingRequest(@NotBlank @Pattern(regexp="[a-zA-Z0-9._-]{1,120}") String queue,
                                 @NotBlank @Pattern(regexp="[a-zA-Z0-9._-]{1,120}") String exchange,
                                 @NotBlank @Size(max=120) @Pattern(regexp="[a-zA-Z0-9.*#_-]+") String routingKey) {}
}
