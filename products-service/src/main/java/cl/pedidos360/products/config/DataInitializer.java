package cl.pedidos360.products.config;

import cl.pedidos360.products.entity.Product;
import cl.pedidos360.products.repository.ProductRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.math.BigDecimal;

@Configuration
public class DataInitializer {

    @Bean
    CommandLineRunner seedProducts(ProductRepository productRepository) {
        return args -> {
            if (productRepository.count() > 0) {
                return;
            }

            Product laptop = new Product();
            laptop.setSku("LAP-001");
            laptop.setName("Laptop Pro 14");
            laptop.setDescription("Laptop empresarial 14 pulgadas");
            laptop.setPrice(new BigDecimal("899990.00"));
            laptop.setStock(25);
            productRepository.save(laptop);

            Product monitor = new Product();
            monitor.setSku("MON-001");
            monitor.setName("Monitor 27 4K");
            monitor.setDescription("Monitor UHD para oficina");
            monitor.setPrice(new BigDecimal("249990.00"));
            monitor.setStock(40);
            productRepository.save(monitor);
        };
    }
}
