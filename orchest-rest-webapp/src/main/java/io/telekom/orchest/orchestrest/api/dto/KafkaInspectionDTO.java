package io.telekom.orchest.orchestrest.api.dto;

import java.util.List;
import lombok.Builder;
import lombok.Data;

/** Container for Kafka inspection-related DTOs used by the Kafka health and diagnostics API. */
public class KafkaInspectionDTO {

  /** Worker connectivity information for a process definition's Kafka topics. */
  @Data
  @Builder
  public static class WorkerInfo {
    private String processDefinitionId;
    private TopicInfo clientWorkerEvent;
    private List<String> clientConsumerGroups;
    private String clientConsumerStatus;
    private TopicInfo serverWorkerEvent;
  }

  /** Topic and consumer-group existence information for a single Kafka topic. */
  @Data
  @Builder
  public static class TopicInfo {
    private String topic;
    private String consumerGroup;
    private boolean topicExists;
    private boolean consumerGroupExists;
  }

  /** Summary of all topics and consumer groups present in the Kafka cluster. */
  @Data
  @Builder
  public static class ClusterInfo {
    private List<String> topics;
    private List<String> consumerGroups;
  }

  /** Aggregated health status of Kafka resources for a single process definition. */
  @Data
  @Builder
  public static class ProcessHealth {
    private String processDefinitionId;
    private ResourceStatus clientWorkerEventTopic;
    private List<String> clientConsumerGroups;
    private ResourceStatus serverWorkerEventTopic;
    private ResourceStatus serverWorkerEventConsumerGroup;
    private boolean healthy;
  }

  /** Existence check result for a named Kafka resource (topic or consumer group). */
  @Data
  @Builder
  public static class ResourceStatus {
    private String name;
    private boolean exists;
  }
}
