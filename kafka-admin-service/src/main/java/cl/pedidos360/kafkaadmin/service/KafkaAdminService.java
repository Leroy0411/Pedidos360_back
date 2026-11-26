package cl.pedidos360.kafkaadmin.service;

import cl.pedidos360.kafkaadmin.api.KafkaAdminRequests.CreateTopicRequest;
import cl.pedidos360.kafkaadmin.api.KafkaAdminRequests.UpdateTopicRequest;
import org.apache.kafka.clients.admin.*;
import org.apache.kafka.clients.consumer.OffsetAndMetadata;
import org.apache.kafka.common.TopicPartition;
import org.apache.kafka.common.config.ConfigResource;
import org.apache.kafka.common.config.TopicConfig;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

@Service
public class KafkaAdminService {
    private final AdminClient adminClient;

    public KafkaAdminService(AdminClient adminClient) {
        this.adminClient = adminClient;
    }

    /** Lista tópicos con particiones, factor de replicación y configuración de retención. */
    public List<Map<String, Object>> listTopics() throws ExecutionException, InterruptedException {
        Set<String> names = adminClient.listTopics().names().get();
        Map<String, TopicDescription> descriptions = adminClient.describeTopics(names).allTopicNames().get();

        List<ConfigResource> resources = names.stream()
                .map(n -> new ConfigResource(ConfigResource.Type.TOPIC, n))
                .collect(Collectors.toList());
        Map<ConfigResource, Config> configs = adminClient.describeConfigs(resources).all().get();

        List<Map<String, Object>> result = new ArrayList<>();
        for (TopicDescription desc : descriptions.values()) {
            int replicas = desc.partitions().isEmpty() ? 0 : desc.partitions().get(0).replicas().size();
            Config cfg = configs.get(new ConfigResource(ConfigResource.Type.TOPIC, desc.name()));

            Map<String, Object> topic = new LinkedHashMap<>();
            topic.put("name", desc.name());
            topic.put("internal", desc.isInternal());
            topic.put("partitions", desc.partitions().size());
            topic.put("replicationFactor", replicas);
            topic.put("retentionMs", configValue(cfg, TopicConfig.RETENTION_MS_CONFIG));
            topic.put("cleanupPolicy", configValue(cfg, TopicConfig.CLEANUP_POLICY_CONFIG));
            topic.put("minInsyncReplicas", configValue(cfg, TopicConfig.MIN_IN_SYNC_REPLICAS_CONFIG));
            result.add(topic);
        }
        result.sort(Comparator.comparing((Map<String, Object> t) -> String.valueOf(t.get("name"))));
        return result;
    }

    private String configValue(Config cfg, String key) {
        if (cfg == null) return "";
        ConfigEntry entry = cfg.get(key);
        return entry == null || entry.value() == null ? "" : entry.value();
    }

    public void createTopic(CreateTopicRequest req) throws ExecutionException, InterruptedException {
        NewTopic topic = new NewTopic(req.name(), req.partitions(), req.replicationFactor());
        if (req.retentionMs() != null && req.retentionMs() > 0) {
            topic.configs(Map.of(TopicConfig.RETENTION_MS_CONFIG, String.valueOf(req.retentionMs())));
        }
        adminClient.createTopics(List.of(topic)).all().get();
    }

    public void updateTopic(String name, UpdateTopicRequest req) throws ExecutionException, InterruptedException {
        if (req.partitions() != null) {
            adminClient.createPartitions(Map.of(name, NewPartitions.increaseTo(req.partitions()))).all().get();
        }
        if (req.retentionMs() != null && req.retentionMs() > 0) {
            ConfigResource resource = new ConfigResource(ConfigResource.Type.TOPIC, name);
            AlterConfigOp op = new AlterConfigOp(
                    new ConfigEntry(TopicConfig.RETENTION_MS_CONFIG, String.valueOf(req.retentionMs())),
                    AlterConfigOp.OpType.SET
            );
            adminClient.incrementalAlterConfigs(Map.of(resource, List.of(op))).all().get();
        }
    }

    public void deleteTopic(String name) throws ExecutionException, InterruptedException {
        adminClient.deleteTopics(List.of(name)).all().get();
    }

    /** Lista consumer groups con estado, miembros, tópicos que consumen y lag total (fin del log - offset confirmado). */
    public List<Map<String, Object>> listConsumerGroups() throws ExecutionException, InterruptedException {
        Collection<ConsumerGroupListing> listings = adminClient.listConsumerGroups().all().get();
        List<String> groupIds = listings.stream().map(ConsumerGroupListing::groupId).collect(Collectors.toList());
        if (groupIds.isEmpty()) return List.of();
        Map<String, ConsumerGroupDescription> described = adminClient.describeConsumerGroups(groupIds).all().get();

        List<Map<String, Object>> result = new ArrayList<>();
        for (ConsumerGroupDescription desc : described.values()) {
            Map<TopicPartition, OffsetAndMetadata> committed = adminClient
                    .listConsumerGroupOffsets(desc.groupId())
                    .partitionsToOffsetAndMetadata().get();

            long lag = 0;
            if (!committed.isEmpty()) {
                Map<TopicPartition, OffsetSpec> latestSpec = new HashMap<>();
                committed.keySet().forEach(tp -> latestSpec.put(tp, OffsetSpec.latest()));
                Map<TopicPartition, ListOffsetsResult.ListOffsetsResultInfo> ends =
                        adminClient.listOffsets(latestSpec).all().get();
                for (Map.Entry<TopicPartition, OffsetAndMetadata> e : committed.entrySet()) {
                    ListOffsetsResult.ListOffsetsResultInfo end = ends.get(e.getKey());
                    if (e.getValue() == null || end == null) continue;
                    lag += Math.max(0, end.offset() - e.getValue().offset());
                }
            }

            List<String> topics = committed.keySet().stream()
                    .map(TopicPartition::topic).distinct().sorted().collect(Collectors.toList());

            Map<String, Object> group = new LinkedHashMap<>();
            group.put("groupId", desc.groupId());
            group.put("state", desc.state().toString());
            group.put("members", desc.members().size());
            group.put("topics", topics);
            group.put("lag", lag);
            result.add(group);
        }
        result.sort(Comparator.comparing((Map<String, Object> g) -> String.valueOf(g.get("groupId"))));
        return result;
    }
}
