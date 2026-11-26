package cl.pedidos360.kafkaadmin.api;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public final class KafkaAdminRequests {
    private KafkaAdminRequests() {}

    public record CreateTopicRequest(
            @NotBlank @Pattern(regexp = "[a-zA-Z0-9._-]{1,120}") String name,
            @Min(1) @Max(32) int partitions,
            @Min(1) @Max(3) short replicationFactor,
            Long retentionMs
    ) {}

    public record UpdateTopicRequest(
            @Min(1) @Max(32) Integer partitions,
            Long retentionMs
    ) {}
}
