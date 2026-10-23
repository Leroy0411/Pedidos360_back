package cl.pedidos360.bff.service;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Map;

@Service
public class DownstreamService {

    private final RestClient ordersRestClient;
    private final RestClient productsRestClient;

    public DownstreamService(
            @Qualifier("ordersRestClient") RestClient ordersRestClient,
            @Qualifier("productsRestClient") RestClient productsRestClient
    ) {
        this.ordersRestClient = ordersRestClient;
        this.productsRestClient = productsRestClient;
    }

    public List<Map<String, Object>> listProducts() {
        return productsRestClient.get()
                .uri("/api/products")
                .retrieve()
                .onStatus(HttpStatusCode::isError, (request, response) -> {
                    throw new ResponseStatusException(response.getStatusCode(), "Error al consultar productos");
                })
                .body(new ParameterizedTypeReference<>() {});
    }

    public Map<String, Object> getProduct(Long id) {
        return productsRestClient.get()
                .uri("/api/products/{id}", id)
                .retrieve()
                .onStatus(HttpStatusCode::isError, (request, response) -> {
                    throw new ResponseStatusException(response.getStatusCode(), "Producto no encontrado");
                })
                .body(new ParameterizedTypeReference<>() {});
    }

    public Map<String, Object> createProduct(Map<String, Object> body) {
        return productsRestClient.post()
                .uri("/api/products")
                .body(body)
                .retrieve()
                .onStatus(HttpStatusCode::isError, (request, response) -> {
                    throw new ResponseStatusException(response.getStatusCode(), "Error al crear producto");
                })
                .body(new ParameterizedTypeReference<>() {});
    }

    public List<Map<String, Object>> listOrders(String customerEmail) {
        RestClient.RequestHeadersSpec<?> spec = ordersRestClient.get().uri(uriBuilder -> {
            var builder = uriBuilder.path("/api/orders");
            if (customerEmail != null && !customerEmail.isBlank()) {
                builder.queryParam("customerEmail", customerEmail);
            }
            return builder.build();
        });

        return spec.retrieve()
                .onStatus(HttpStatusCode::isError, (request, response) -> {
                    throw new ResponseStatusException(response.getStatusCode(), "Error al consultar pedidos");
                })
                .body(new ParameterizedTypeReference<>() {});
    }

    public Map<String, Object> getOrder(Long id) {
        return ordersRestClient.get()
                .uri("/api/orders/{id}", id)
                .retrieve()
                .onStatus(HttpStatusCode::isError, (request, response) -> {
                    throw new ResponseStatusException(response.getStatusCode(), "Pedido no encontrado");
                })
                .body(new ParameterizedTypeReference<>() {});
    }

    public Map<String, Object> createOrder(Map<String, Object> body) {
        return ordersRestClient.post()
                .uri("/api/orders")
                .body(body)
                .retrieve()
                .onStatus(HttpStatusCode::isError, (request, response) -> {
                    throw new ResponseStatusException(response.getStatusCode(), "Error al crear pedido");
                })
                .body(new ParameterizedTypeReference<>() {});
    }

    public Map<String, Object> confirmOrder(Long id) {
        return ordersRestClient.put()
                .uri("/api/orders/{id}/confirm", id)
                .retrieve()
                .onStatus(HttpStatusCode::isError, (request, response) -> {
                    throw new ResponseStatusException(response.getStatusCode(), "Error al confirmar pedido");
                })
                .body(new ParameterizedTypeReference<>() {});
    }
}
