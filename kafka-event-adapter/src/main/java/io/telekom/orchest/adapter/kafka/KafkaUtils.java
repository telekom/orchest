package io.telekom.orchest.adapter.kafka;

import static io.telekom.orchest.adapter.kafka.client.TopicConstant.*;

import io.telekom.orchest.adapter.kafka.config.OrchestKafkaProperties;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * Utility class for Kafka topic and configuration management. Provides methods to generate topic
 * names with environment suffixes, manage consumer groups, and determine replication and partition
 * counts based on environment.
 */
@Component
@RequiredArgsConstructor
public class KafkaUtils {

  @Value("${spring.profiles.active:LOCAL}")
  private String activeProfile;

  private final OrchestKafkaProperties orchestKafkaProperties;

  /** Separator character used in topic names. */
  public static final String SEPARATOR = "_";

  /** Current environment name (e.g., LOCAL, TEST, PROD). */
  public static String ENVIRONMENT;

  private static OrchestKafkaProperties staticOrchestKafkaProperties;

  /**
   * Initializes static properties after bean construction. Sets up environment and profile
   * variables based on active Spring profile.
   */
  @PostConstruct
  public void init() {
    staticOrchestKafkaProperties = orchestKafkaProperties;
    ENVIRONMENT =
        StringUtils.upperCase(activeProfile == null ? "LOCAL" : activeProfile)
            .replace("-", SEPARATOR);
  }

  /**
   * Appends environment suffix to a topic name.
   *
   * @param topic The base topic name.
   * @return The topic name with environment suffix appended (if not LOCAL).
   */
  public static String getTopicWithEnvSuffix(String topic) {
    return topic + getKafkaSuffix();
  }

  /**
   * Generates the client worker event topic name for a specific process. The topic name includes
   * the process ID and environment suffix.
   *
   * @param processId The process definition ID.
   * @return The formatted topic name for client worker events.
   */
  public static String getClientWorkerEventTopic(String processId) {
    return getTopicWithEnvSuffix(
        CLIENT_WORKER_EVENT_TOPIC_PREFIX
            + StringUtils.upperCase(processId.replace("-", SEPARATOR)));
  }

  /**
   * Generates the canary variant of the client worker event topic for blue/green deployments.
   *
   * @param processId the process definition ID
   * @return the canary topic name with {@code _CANARY} suffix
   */
  public static String getClientWorkerEventTopicForCanary(String processId) {
    return getClientWorkerEventTopic(processId) + "_CANARY";
  }

  /**
   * Generates the client worker event topic name for a Common process. The topic name include
   * environment suffix.
   *
   * @return The formatted topic name for client common worker events.
   */
  public static String getClientCommonWorkerEventTopic() {
    return CLIENT_COMMON_WORKER_EVENT_TOPIC + getKafkaSuffix();
  }

  /**
   * Generates the server worker event topic name for a specific process. The topic name includes
   * the process ID and environment suffix.
   *
   * @param processId The process definition ID.
   * @return The formatted topic name for client worker events.
   */
  public static String getServerWorkerEventTopic(String processId) {
    return getTopicWithEnvSuffix(
        SERVER_WORKER_EVENT_TOPIC_PREFIX
            + StringUtils.upperCase(processId.replace("-", SEPARATOR)));
  }

  /**
   * Gets the incident event topic name with environment suffix.
   *
   * @return The formatted topic name for server incident events.
   */
  public static String getIncidentEventTopic() {
    return getTopicWithEnvSuffix(SERVER_INCIDENT_EVENT);
  }

  /**
   * Gets the incident resolution event topic name with environment suffix.
   *
   * @return The formatted topic name for server incident resolution events.
   */
  public static String getIncidentResolutionEventTopic() {
    return getTopicWithEnvSuffix(SERVER_INCIDENT_RESOLUTION_EVENT);
  }

  /**
   * Gets the connector task dispatch event topic name with environment suffix.
   *
   * @return The formatted topic name for connector task events.
   */
  public static String getConnectorTaskTopic() {
    return getTopicWithEnvSuffix(CONNECTOR_TASK_EVENT_TOPIC);
  }

  /**
   * Generates a consumer group ID with environment suffix.
   *
   * @param topic The base topic name.
   * @return The consumer group ID in the format: {topic}_CONSUMER_GROUP{envSuffix}.
   */
  public static String getGroupIdWithEnvSuffix(String topic) {
    return topic + SEPARATOR + "CONSUMER_GROUP" + getKafkaSuffix();
  }

  /**
   * Determines the replication factor for Kafka topics based on environment. Local and test
   * environments use replication factor of 1, others use configured value.
   *
   * @return The replication count (1 for LOCAL/TEST, configured value otherwise).
   */
  public static int getReplicationCount() {
    if ("LOCAL".equals(ENVIRONMENT) || "TEST".equals(ENVIRONMENT)) {
      return 1;
    }
    return staticOrchestKafkaProperties != null
        ? staticOrchestKafkaProperties.getReplicaCount()
        : 1;
  }

  /**
   * Determines the partition count for Kafka topics based on environment. Local and test
   * environments use 1 partition, others use 10 partitions.
   *
   * @return The partition count (1 for LOCAL/TEST, 10 otherwise).
   */
  public static int getPartitionCount() {
    if ("LOCAL".equals(ENVIRONMENT) || "TEST".equals(ENVIRONMENT)) {
      return 1;
    }
    return staticOrchestKafkaProperties != null
        ? staticOrchestKafkaProperties.getDefaultPartitions()
        : 1;
  }

  /**
   * Gets the environment suffix for topic names. Returns empty string for LOCAL (or unset)
   * environment, otherwise returns {@code _}{@link OrchestKafkaProperties#getSuffix() configured
   * suffix} when a suffix is set.
   *
   * @return The environment suffix string, or empty string for LOCAL.
   */
  public static String getKafkaSuffix() {
    if (staticOrchestKafkaProperties == null) {
      return "";
    }
    String configuredSuffix = staticOrchestKafkaProperties.getSuffix();
    if (StringUtils.isNotBlank(configuredSuffix)) {
      return SEPARATOR + configuredSuffix;
    }
    return "";
  }
}
