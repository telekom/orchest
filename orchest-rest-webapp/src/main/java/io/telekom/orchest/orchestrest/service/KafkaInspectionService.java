package io.telekom.orchest.orchestrest.service;

import io.telekom.orchest.adapter.kafka.KafkaUtils;
import io.telekom.orchest.api.core.adapters.data.model.ProcessDefinition;
import io.telekom.orchest.orchestrest.api.dto.KafkaInspectionDTO.ClusterInfo;
import io.telekom.orchest.orchestrest.api.dto.KafkaInspectionDTO.ProcessHealth;
import io.telekom.orchest.orchestrest.api.dto.KafkaInspectionDTO.ResourceStatus;
import io.telekom.orchest.orchestrest.api.dto.KafkaInspectionDTO.TopicInfo;
import io.telekom.orchest.orchestrest.api.dto.KafkaInspectionDTO.WorkerInfo;
import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.admin.AdminClient;
import org.apache.kafka.clients.admin.ConsumerGroupListing;
import org.apache.kafka.clients.consumer.OffsetAndMetadata;
import org.apache.kafka.common.TopicPartition;
import org.springframework.stereotype.Service;

/** Service for inspecting Kafka topic/consumer-group state and assessing per-process health. */
@Slf4j
@Service
@RequiredArgsConstructor
public class KafkaInspectionService {

  private static final String ORCHEST_PREFIX = "ORCHEST_";

  private final AdminClient adminClient;
  private final DataInteractionService dataInteractionService;

  public WorkerInfo getProcessInfo(String processDefinitionId) {
    String clientTopic = KafkaUtils.getClientWorkerEventTopic(processDefinitionId);
    String serverTopic = KafkaUtils.getServerWorkerEventTopic(processDefinitionId);
    String serverGroup = KafkaUtils.getGroupIdWithEnvSuffix(serverTopic);

    try {
      Set<String> existingTopics = adminClient.listTopics().names().get();
      Set<String> existingGroups = listConsumerGroupIds();

      List<String> clientConsumerGroups = findConsumerGroupsForTopic(clientTopic);
      String clientConsumerStatus = clientConsumerGroups.isEmpty() ? "ABSENT" : "PRESENT";

      return WorkerInfo.builder()
          .processDefinitionId(processDefinitionId)
          .clientWorkerEvent(
              TopicInfo.builder()
                  .topic(clientTopic)
                  .topicExists(existingTopics.contains(clientTopic))
                  .build())
          .clientConsumerGroups(clientConsumerGroups)
          .clientConsumerStatus(clientConsumerStatus)
          .serverWorkerEvent(
              TopicInfo.builder()
                  .topic(serverTopic)
                  .consumerGroup(serverGroup)
                  .topicExists(existingTopics.contains(serverTopic))
                  .consumerGroupExists(existingGroups.contains(serverGroup))
                  .build())
          .build();

    } catch (InterruptedException | ExecutionException e) {
      log.error("Failed to query Kafka cluster for worker info: {}", e.getMessage(), e);
      Thread.currentThread().interrupt();
      throw new RuntimeException("Kafka cluster unavailable: " + e.getMessage(), e);
    }
  }

  public ClusterInfo getClusterInfo() {
    try {
      List<String> topics =
          adminClient.listTopics().names().get().stream()
              .filter(t -> t.startsWith(ORCHEST_PREFIX))
              .sorted()
              .toList();

      List<String> groups =
          listConsumerGroupIds().stream()
              .filter(g -> g.startsWith(ORCHEST_PREFIX))
              .sorted()
              .toList();

      return ClusterInfo.builder().topics(topics).consumerGroups(groups).build();

    } catch (InterruptedException | ExecutionException e) {
      log.error("Failed to query Kafka cluster info: {}", e.getMessage(), e);
      Thread.currentThread().interrupt();
      throw new RuntimeException("Kafka cluster unavailable: " + e.getMessage(), e);
    }
  }

  public List<ProcessHealth> getProcessHealth() {
    List<ProcessDefinition> definitions = dataInteractionService.getProcessDefinitions();

    try {
      Set<String> existingTopics = adminClient.listTopics().names().get();
      Set<String> existingGroups = listConsumerGroupIds();

      return definitions.stream()
          .map(def -> buildProcessHealth(def.getDefinitionId(), existingTopics, existingGroups))
          .toList();

    } catch (InterruptedException | ExecutionException e) {
      log.error("Failed to query Kafka cluster for process health: {}", e.getMessage(), e);
      Thread.currentThread().interrupt();
      throw new RuntimeException("Kafka cluster unavailable: " + e.getMessage(), e);
    }
  }

  private ProcessHealth buildProcessHealth(
      String processDefinitionId, Set<String> existingTopics, Set<String> existingGroups) {
    String clientTopic = KafkaUtils.getClientWorkerEventTopic(processDefinitionId);
    String serverTopic = KafkaUtils.getServerWorkerEventTopic(processDefinitionId);
    String serverGroup = KafkaUtils.getGroupIdWithEnvSuffix(serverTopic);

    ResourceStatus clientTopicStatus =
        ResourceStatus.builder()
            .name(clientTopic)
            .exists(existingTopics.contains(clientTopic))
            .build();
    ResourceStatus serverTopicStatus =
        ResourceStatus.builder()
            .name(serverTopic)
            .exists(existingTopics.contains(serverTopic))
            .build();
    ResourceStatus serverGroupStatus =
        ResourceStatus.builder()
            .name(serverGroup)
            .exists(existingGroups.contains(serverGroup))
            .build();

    List<String> clientConsumerGroups;
    try {
      clientConsumerGroups = findConsumerGroupsForTopic(clientTopic);
    } catch (Exception e) {
      log.warn(
          "Failed to find consumer groups for client topic {}: {}", clientTopic, e.getMessage());
      clientConsumerGroups = Collections.emptyList();
    }

    boolean healthy =
        clientTopicStatus.isExists()
            && serverTopicStatus.isExists()
            && !clientConsumerGroups.isEmpty()
            && serverGroupStatus.isExists();

    return ProcessHealth.builder()
        .processDefinitionId(processDefinitionId)
        .clientWorkerEventTopic(clientTopicStatus)
        .clientConsumerGroups(clientConsumerGroups)
        .serverWorkerEventTopic(serverTopicStatus)
        .serverWorkerEventConsumerGroup(serverGroupStatus)
        .healthy(healthy)
        .build();
  }

  private List<String> findConsumerGroupsForTopic(String topic)
      throws InterruptedException, ExecutionException {
    Set<String> allGroupIds = listConsumerGroupIds();
    List<String> matchingGroups = new ArrayList<>();

    for (String groupId : allGroupIds) {
      try {
        Map<TopicPartition, OffsetAndMetadata> offsets =
            adminClient.listConsumerGroupOffsets(groupId).partitionsToOffsetAndMetadata().get();
        boolean subscribedToTopic =
            offsets.keySet().stream().anyMatch(tp -> tp.topic().equals(topic));
        if (subscribedToTopic) {
          matchingGroups.add(groupId);
        }
      } catch (Exception e) {
        log.debug("Could not inspect offsets for group {}: {}", groupId, e.getMessage());
      }
    }

    return matchingGroups;
  }

  private Set<String> listConsumerGroupIds() throws InterruptedException, ExecutionException {
    return adminClient.listConsumerGroups().all().get().stream()
        .map(ConsumerGroupListing::groupId)
        .collect(Collectors.toSet());
  }
}
