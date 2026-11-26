package cl.pedidos360.kafkaadmin.service;

import cl.pedidos360.kafkaadmin.api.KafkaAdminRequests.CreateTopicRequest;
import cl.pedidos360.kafkaadmin.api.KafkaAdminRequests.UpdateTopicRequest;
import org.apache.kafka.clients.admin.*;
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

    public List> listTopics() throws ExecutionException, InterruptedException {
        Set<String> names = adminClient.listTopics().names().get();
        Map<String, TopicDescription> descriptions = adminClient.describeTopics(names).allTopicNames().get();
        List> result = new ArrayList<>();
        for (TopicDescription desc : descriptions.values()) {
            int replicas = desc.partitions().isEmpty() ? 0 : desc.partitions().get(0).replicas().size();
            result.add(Map.of(
                    "name", desc.name(),
                    "internal", desc.isInternal(),
                    "partitions", desc.partitions().size(),
                    "replicationFactor", replicas
            ));
        }
        return result;
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

    public List> listConsumerGroups() throws ExecutionException, InterruptedException {
        Collection<ConsumerGroupListing> listings = adminClient.listConsumerGroups().all().get();
        List<String> groupIds = listings.stream().map(ConsumerGroupListing::groupId).collect(Collectors.toList());
        if (groupIds.isEmpty()) return List.of();
        Map<String, ConsumerGroupDescription> described = adminClient.describeConsumerGroups(groupIds).all().get();
        List> result = new ArrayList<>();
        for (ConsumerGroupDescription desc : described.values()) {
            result.add(Map.of(
                    "groupId", desc.groupId(),
                    "state", desc.state().toString(),
                    "members", desc.members().size()
            ));
        }
        return result;
    }
}
