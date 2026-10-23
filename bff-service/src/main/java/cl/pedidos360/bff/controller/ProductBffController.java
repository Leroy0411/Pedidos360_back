package cl.pedidos360.bff.controller;

import cl.pedidos360.bff.service.DownstreamService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/products")
public class ProductBffController {

    private final DownstreamService downstreamService;

    public ProductBffController(DownstreamService downstreamService) {
        this.downstreamService = downstreamService;
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('USER','ADMIN')")
    public List<Map<String, Object>> list() {
        return downstreamService.listProducts();
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('USER','ADMIN')")
    public Map<String, Object> getById(@PathVariable Long id) {
        return downstreamService.getProduct(id);
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public Map<String, Object> create(@RequestBody Map<String, Object> body) {
        return downstreamService.createProduct(body);
    }
}
