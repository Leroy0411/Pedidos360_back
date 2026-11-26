package cl.pedidos360.bff.controller;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestClient;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/kafka-admin")
public class KafkaAdminBffController {

    private final RestClient kafkaAdminRestClient;

    public KafkaAdminBffController(@Qualifier("kafkaAdminRestClient") RestClient kafkaAdminRestClient) {
        this.kafkaAdminRestClient = kafkaAdminRestClient;
    }

    @GetMapping("/health")
    public Map<String, Object> health() {
        return kafkaAdminRestClient.get().uri("/api/kafka-admin/health").retrieve().body(new ParameterizedTypeReference<>() {});
    }

    @GetMapping("/topics")
    public List> listTopics() {
        return kafkaAdminRestClient.get().uri("/api/kafka-admin/topics").retrieve().body(new ParameterizedTypeReference<>() {});
    }

    @PostMapping("/topics")
    public ResponseEntity> createTopic(@RequestBody Map<String, Object> body) {
        Map<String, Object> res = kafkaAdminRestClient.post().uri("/api/kafka-admin/topics").body(body).retrieve().body(new ParameterizedTypeReference<>() {});
        return ResponseEntity.status(201).body(res);
    }

    @PatchMapping("/topics/{name}")
    public Map<String, Object> updateTopic(@PathVariable String name, @RequestBody Map<String, Object> body) {
        return kafkaAdminRestClient.patch().uri("/api/kafka-admin/topics/{name}", name).body(body).retrieve().body(new ParameterizedTypeReference<>() {});
    }

    @DeleteMapping("/topics/{name}")
    public ResponseEntity<Void> deleteTopic(@PathVariable String name) {
        kafkaAdminRestClient.delete().uri("/api/kafka-admin/topics/{name}", name).retrieve().toBodilessEntity();
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/consumer-groups")
    public List> listConsumerGroups() {
        return kafkaAdminRestClient.get().uri("/api/kafka-admin/consumer-groups").retrieve().body(new ParameterizedTypeReference<>() {});
    }
}
