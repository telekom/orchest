package io.telekom.orchest.config;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Configuration properties for the pure Java OrchesT Client. This is a plain Java version without
 * Spring dependencies. Controls Kafka settings, worker behavior, security, and alerting.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrchestClientProperties {

  /** Enable or disable worker consumers */
  @Builder.Default private boolean enableWorkers = true;

  /** Kafka Bootstrap servers (comma-separated list) */
  private String bootstrapServers;

  /** List of process IDs for which workers are implemented */
  @Builder.Default private List<String> processIds = new ArrayList<>();

  /** Consumer poll timeout in milliseconds */
  @Builder.Default private long pollTimeoutMs = 30_000;

  /** Number of worker threads per consumer (concurrency level) */
  @Builder.Default private int workerThreadCount = 3;

  /** Consumer group ID for this client */
  private String consumerGroupId;

  /** Client ID prefix for Kafka connections */
  private String clientId;

  /** Application name (used for consumer group naming) */
  private String applicationName;

  private String kafkaSuffix;

  /** Map to log specific key:value pairs from variables */
  private Map<String, String> logVariables;

  /** AWS-specific configuration */
  @Builder.Default private Encryption encryption = new Encryption();

  /** REST API configuration */
  private Rest rest;

  /** Kafka consumer configuration overrides */
  @Builder.Default private KafkaConsumerConfig kafkaConsumer = new KafkaConsumerConfig();

  /** Kafka producer configuration overrides */
  @Builder.Default private KafkaProducerConfig kafkaProducer = new KafkaProducerConfig();

  /** AWS KMS encryption configuration for Kafka payload encryption. */
  @Data
  @Builder
  @AllArgsConstructor
  @NoArgsConstructor
  public static class Encryption {
    private boolean enableEncryption;
    private String kmsKeyARN;
    private String iamRoleARN;
    @Builder.Default private String region = "eu-central-1";
  }

  /** REST API configuration */
  @Data
  @Builder
  @NoArgsConstructor
  @AllArgsConstructor
  public static class Rest {
    /** Base URL for OrchesT REST API */
    private String url;

    /** Connection timeout in milliseconds */
    @Builder.Default private int connectTimeout = 10_000;

    /** Read timeout in milliseconds */
    @Builder.Default private int readTimeout = 30_000;
  }

  /** Incident alerting configuration */
  @Data
  @Builder
  @NoArgsConstructor
  @AllArgsConstructor
  public static class IncidentAlert {
    /** Enable incident alerting */
    @Builder.Default private boolean enabled = false;

    /** MS Teams webhook URL for incident alerts */
    private String incidentAlertChannel;
  }

  /** Kafka consumer-specific configuration */
  @Data
  @Builder
  @NoArgsConstructor
  @AllArgsConstructor
  public static class KafkaConsumerConfig {
    /** Maximum number of records returned in a single poll */
    @Builder.Default private int maxPollRecords = 100;

    /**
     * Maximum time between polls before consumer is considered failed (ms). Must be larger than the
     * time to process max.poll.records; default 5 min.
     */
    @Builder.Default private int maxPollIntervalMs = 300_000;

    /** Session timeout for consumer group management */
    @Builder.Default private int sessionTimeoutMs = 45_000;

    /** Heartbeat interval for consumer group coordination */
    @Builder.Default private int heartbeatIntervalMs = 2_000;

    /** Auto offset reset strategy (earliest, latest, none) */
    @Builder.Default private String autoOffsetReset = "latest";

    /** Enable auto commit of offsets */
    @Builder.Default private boolean enableAutoCommit = true;

    /** Auto commit interval in milliseconds */
    @Builder.Default private int autoCommitIntervalMs = 5_000;

    /** Fetch minimum bytes */
    @Builder.Default private int fetchMinBytes = 1;

    /** Fetch maximum wait time in milliseconds */
    @Builder.Default private int fetchMaxWaitMs = 500;

    /** Maximum partition fetch bytes */
    @Builder.Default private int maxPartitionFetchBytes = 1048576; // 1MB

    /**
     * Optional static group instance id. If set, this consumer uses static membership (required by
     * Kafka 3.x when broker requires a valid member id before joining). If not set, a default
     * per-consumer id (clientId-topic) is used.
     */
    private String groupInstanceId;

    /** Custom properties for advanced configuration */
    private Properties additionalProperties;

    /** Converts this config to Kafka Properties */
    public Properties toProperties() {
      Properties props = new Properties();
      if (additionalProperties != null) {
        props.putAll(additionalProperties);
      }
      props.put("max.poll.records", String.valueOf(maxPollRecords));
      props.put("max.poll.interval.ms", String.valueOf(maxPollIntervalMs));
      props.put("session.timeout.ms", String.valueOf(sessionTimeoutMs));
      props.put("heartbeat.interval.ms", String.valueOf(heartbeatIntervalMs));
      props.put("auto.offset.reset", autoOffsetReset);
      props.put("enable.auto.commit", String.valueOf(enableAutoCommit));
      props.put("auto.commit.interval.ms", String.valueOf(autoCommitIntervalMs));
      props.put("fetch.min.bytes", String.valueOf(fetchMinBytes));
      props.put("fetch.max.wait.ms", String.valueOf(fetchMaxWaitMs));
      props.put("max.partition.fetch.bytes", String.valueOf(maxPartitionFetchBytes));
      return props;
    }
  }

  /** Kafka producer-specific configuration */
  @Data
  @Builder
  @NoArgsConstructor
  @AllArgsConstructor
  public static class KafkaProducerConfig {
    /** Acknowledgment mode (0, 1, all) */
    @Builder.Default private String acks = "all";

    /** Number of retries for failed sends */
    @Builder.Default private int retries = 3;

    /** Batch size in bytes */
    @Builder.Default private int batchSize = 16384;

    /** Linger time in milliseconds */
    @Builder.Default private int lingerMs = 10;

    /** Buffer memory in bytes */
    @Builder.Default private long bufferMemory = 33554432; // 32MB

    /** Compression type (none, gzip, snappy, lz4, zstd) */
    @Builder.Default private String compressionType = "lz4";

    /** Maximum in-flight requests per connection */
    @Builder.Default private int maxInFlightRequestsPerConnection = 5;

    /** Request timeout in milliseconds */
    @Builder.Default private int requestTimeoutMs = 30_000;

    /** Idempotence enabled for exactly-once semantics */
    @Builder.Default private boolean enableIdempotence = true;

    /** Custom properties for advanced configuration */
    private Properties additionalProperties;

    /** Converts this config to Kafka Properties */
    public Properties toProperties() {
      Properties props = new Properties();
      if (additionalProperties != null) {
        props.putAll(additionalProperties);
      }
      props.put("acks", acks);
      props.put("retries", String.valueOf(retries));
      props.put("batch.size", String.valueOf(batchSize));
      props.put("linger.ms", String.valueOf(lingerMs));
      props.put("buffer.memory", String.valueOf(bufferMemory));
      props.put("compression.type", compressionType);
      props.put(
          "max.in.flight.requests.per.connection",
          String.valueOf(maxInFlightRequestsPerConnection));
      props.put("request.timeout.ms", String.valueOf(requestTimeoutMs));
      props.put("enable.idempotence", String.valueOf(enableIdempotence));
      return props;
    }
  }

  /** Validates the configuration and sets defaults where needed */
  public void validate() {
    if (bootstrapServers == null || bootstrapServers.trim().isEmpty()) {
      throw new IllegalStateException("bootstrapServers must be configured");
    }
    if (processIds == null || processIds.isEmpty()) {
      throw new IllegalStateException("processIds must be configured with at least one process ID");
    }
    if (applicationName == null || applicationName.trim().isEmpty()) {
      throw new IllegalStateException("applicationName must be configured");
    }

    // Set default consumer group if not provided
    if (consumerGroupId == null || consumerGroupId.trim().isEmpty()) {
      consumerGroupId =
          "ORCHEST_JAVA_CLIENT_"
              + applicationName.toUpperCase().replace("-", "_")
              + "_CONSUMER"
              + getKafkaSuffix();
    }

    // Set default client ID if not provided
    if (clientId == null || clientId.trim().isEmpty()) {
      clientId =
          "ORCHEST_JAVA_CLIENT_"
              + applicationName.toUpperCase().replace("-", "_")
              + getKafkaSuffix();
    }
  }

  /**
   * Returns the normalized Kafka topic suffix (uppercased, hyphens replaced with underscores).
   *
   * @return the topic suffix, or empty string if not configured
   */
  public String getKafkaSuffix() {
    if (kafkaSuffix == null || kafkaSuffix.trim().isEmpty()) {
      return "";
    } else {
      return "_" + kafkaSuffix.toUpperCase().replace("-", "_");
    }
  }
}
