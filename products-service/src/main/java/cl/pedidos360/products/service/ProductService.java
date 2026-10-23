package cl.pedidos360.products.service;

import cl.pedidos360.products.dto.ProductRequest;
import cl.pedidos360.products.dto.ProductResponse;
import cl.pedidos360.products.entity.Product;
import cl.pedidos360.products.repository.ProductRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
public class ProductService {

    private final ProductRepository productRepository;

    public ProductService(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    public List<ProductResponse> findAllActive() {
        return productRepository.findByActiveTrueOrderByNameAsc().stream()
                .map(ProductResponse::from)
                .toList();
    }

    public ProductResponse findById(Long id) {
        return productRepository.findById(id)
                .map(ProductResponse::from)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Producto no encontrado"));
    }

    public ProductResponse create(ProductRequest request) {
        productRepository.findBySku(request.sku()).ifPresent(existing -> {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "SKU ya registrado");
        });

        Product product = new Product();
        product.setSku(request.sku());
        product.setName(request.name());
        product.setDescription(request.description());
        product.setPrice(request.price());
        product.setStock(request.stock());
        product.setActive(true);

        return ProductResponse.from(productRepository.save(product));
    }
}
