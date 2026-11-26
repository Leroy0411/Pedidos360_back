package cl.pedidos360.kafkaadmin.api;

import cl.pedidos360.kafkaadmin.api.KafkaAdminRequests.CreateTopicRequest;
import cl.pedidos360.kafkaadmin.api.KafkaAdminRequests.UpdateTopicRequest;
import cl.pedidos360.kafkaadmin.service.KafkaAdminService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Pattern;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@Validated
@RequestMapping("/api/kafka-admin")
public class KafkaAdminController {
    private final KafkaAdminService service;

    public KafkaAdminController(KafkaAdminService service) {
        this.service = service;
    }

    @GetMapping("/health")
    public Map<String, String> health() {
        return Map.of("status", "UP", "service", "kafka-admin-service");
    }

    @GetMapping("/topics")
    public ResponseEntity<?> listTopics() throws Exception {
        return ResponseEntity.ok(service.listTopics());
    }

    @PostMapping("/topics")
    public ResponseEntity<?> createTopic(@Valid @RequestBody CreateTopicRequest req) throws Exception {
        service.createTopic(req);
        return ResponseEntity.status(HttpStatus.CREATED).body(Map.of(
                "name", req.name(),
                "partitions", req.partitions(),
                "replicationFactor", req.replicationFactor()
        ));
    }

    @PatchMapping("/topics/{name}")
    public ResponseEntity<?> updateTopic(@PathVariable @Pattern(regexp = "[a-zA-Z0-9._-]{1,120}") String name,
                                         @Valid @RequestBody UpdateTopicRequest req) throws Exception {
        service.updateTopic(name, req);
        return ResponseEntity.ok(Map.of("updated", name));
    }

    @DeleteMapping("/topics/{name}")
    public ResponseEntity<Void> deleteTopic(@PathVariable @Pattern(regexp = "[a-zA-Z0-9._-]{1,120}") String name) throws Exception {
        service.deleteTopic(name);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/consumer-groups")
    public ResponseEntity<?> listConsumerGroups() throws Exception {
        return ResponseEntity.ok(service.listConsumerGroups());
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, String>> handleError(Exception ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("error", ex.getMessage()));
    }
}
