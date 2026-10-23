package cl.pedidos360.rabbitadmin.api;

import cl.pedidos360.rabbitadmin.api.AdminRequests.*;
import cl.pedidos360.rabbitadmin.service.RabbitAdminService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Pattern;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@Validated
@RequestMapping("/api/admin")
public class RabbitAdminController {
    private final RabbitAdminService service;

    public RabbitAdminController(RabbitAdminService service) { this.service = service; }

    @PostMapping("/queues")
    public ResponseEntity<?> createQueue(@Valid @RequestBody QueueRequest input) {
        service.createQueue(input);
        return ResponseEntity.status(HttpStatus.CREATED).body(Map.of("name", input.name()));
    }

    @DeleteMapping("/queues/{name}")
    public ResponseEntity<Void> deleteQueue(@PathVariable @Pattern(regexp="[a-zA-Z0-9._-]{1,120}") String name) {
        return service.deleteQueue(name) ? ResponseEntity.noContent().build() : ResponseEntity.notFound().build();
    }

    @PostMapping("/exchanges")
    public ResponseEntity<?> createExchange(@Valid @RequestBody ExchangeRequest input) {
        service.createExchange(input);
        return ResponseEntity.status(HttpStatus.CREATED).body(Map.of("name", input.name()));
    }

    @DeleteMapping("/exchanges/{name}")
    public ResponseEntity<Void> deleteExchange(@PathVariable @Pattern(regexp="[a-zA-Z0-9._-]{1,120}") String name) {
        return service.deleteExchange(name) ? ResponseEntity.noContent().build() : ResponseEntity.notFound().build();
    }

    @PostMapping("/bindings")
    public ResponseEntity<Void> bind(@Valid @RequestBody BindingRequest input) {
        service.createBinding(input);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @DeleteMapping("/bindings")
    public ResponseEntity<Void> unbind(@Valid @RequestBody BindingRequest input) {
        service.deleteBinding(input);
        return ResponseEntity.noContent().build();
    }
}
