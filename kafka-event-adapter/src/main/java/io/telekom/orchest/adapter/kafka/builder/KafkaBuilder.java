package io.telekom.orchest.adapter.kafka.builder;

import io.micrometer.core.instrument.MeterRegistry;
import java.time.Duration;
import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.CommonClientConfigs;
import org.apache.kafka.clients.admin.*;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.config.ConfigResource;
import org.apache.kafka.common.errors.TopicExistsException;
import org.springframework.boot.kafka.autoconfigure.KafkaProperties;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.*;
import org.springframework.util.unit.DataSize;

/**
 * Shared OrchesT defaults for Spring {@link KafkaProperties}. Does not raise socket connection
 * setup timeouts above Kafka defaults, so MSK/network/advertised-listener problems surface clearly;
 * tune {@code orchest.kafka.config.*} when an environment genuinely needs longer handshakes.
 */
@Slf4j
@NoArgsConstructor
public class KafkaBuilder<K, V> {

  /** Shared defaults (ms / enum) for producer, consumer, and admin clients. */
  private static final String DNS_LOOKUP_USE_ALL_IPS = "use_all_dns_ips";

  private static final String MS_500 = "500";
  private static final String MS_1K = "1000";
  private static final String MS_10K = "10000";
  private static final String MS_60K = "60000";
  private static final String MS_120K = "120000";
  private static final String MS_180K = "180000";
  private static final String MS_300K = "300000";

  /**
   * Builds a Kafka producer factory with OrchesT defaults and Micrometer metrics.
   *
   * @param kafkaProperties Spring Kafka properties to configure the producer
   * @param clientId the Kafka client ID
   * @param meterRegistry Micrometer registry for producer metrics
   * @return configured producer factory
   */
  public ProducerFactory<K, V> buildKafkaProducerFactory(
      KafkaProperties kafkaProperties, String clientId, MeterRegistry meterRegistry) {
    loadDefaultProducerProperties(kafkaProperties, clientId);
    DefaultKafkaProducerFactory<K, V> factory =
        new DefaultKafkaProducerFactory<>(kafkaProperties.buildProducerProperties());
    factory.addListener(new MicrometerProducerListener<>(meterRegistry));
    return factory;
  }

  /**
   * Builds a KafkaTemplate with observation enabled for tracing.
   *
   * @param producerFactory the producer factory to back the template
   * @return configured KafkaTemplate
   */
  public KafkaTemplate<K, V> buildKafkaTemplate(ProducerFactory<K, V> producerFactory) {
    KafkaTemplate<K, V> kafkaTemplate = new KafkaTemplate<>(producerFactory);
    kafkaTemplate.setObservationEnabled(true);
    return kafkaTemplate;
  }

  /**
   * Builds a KafkaAdmin with OrchesT defaults for topic management operations.
   *
   * @param kafkaProperties Spring Kafka properties
   * @param clientId the Kafka client ID
   * @return configured KafkaAdmin
   */
  public KafkaAdmin buildKafkaAdmin(KafkaProperties kafkaProperties, String clientId) {
    loadDefaultKafkaAdminClientProperties(kafkaProperties);
    KafkaAdmin admin = new KafkaAdmin(kafkaProperties.buildAdminProperties());
    loadDefaultKafkaAdminProperties(admin, kafkaProperties, clientId);

    return admin;
  }

  /**
   * Builds a concurrent Kafka listener container factory with OrchesT consumer defaults.
   *
   * @param kafkaProperties Spring Kafka properties
   * @param clientId the Kafka client ID
   * @param groupId the consumer group ID
   * @param meterRegistry Micrometer registry for consumer metrics (nullable)
   * @return configured listener container factory
   */
  public ConcurrentKafkaListenerContainerFactory<K, V> buildConcurrentKafkaListenerContainerFactory(
      KafkaProperties kafkaProperties,
      String clientId,
      String groupId,
      MeterRegistry meterRegistry) {
    KafkaProperties.Consumer consumer = kafkaProperties.getConsumer();
    loadDefaultKafkaConsumerProperties(consumer, clientId, groupId);
    DefaultKafkaConsumerFactory<K, V> consumerFactory =
        new DefaultKafkaConsumerFactory<>(kafkaProperties.buildConsumerProperties());

    if (meterRegistry != null) {
      consumerFactory.addListener(new MicrometerConsumerListener<>(meterRegistry));
    }

    ConcurrentKafkaListenerContainerFactory<K, V> containerFactory =
        new ConcurrentKafkaListenerContainerFactory<>();
    containerFactory.setConsumerFactory(consumerFactory);
    return containerFactory;
  }

  private void loadDefaultProducerProperties(KafkaProperties kafkaProperties, String ClientId) {
    KafkaProperties.Producer producer = kafkaProperties.getProducer();

    if (producer.getAcks() == null) {
      producer.setAcks("all");
    }
    if (producer.getRetries() == null) {
      producer.setRetries(Integer.MAX_VALUE);
    }
    if (producer.getBatchSize() == null) {
      producer.setBatchSize(DataSize.ofBytes(32_768));
    }
    if (producer.getBufferMemory() == null) {
      producer.setBufferMemory(DataSize.ofBytes(67_108_864));
    }

    if (ClientId != null) {
      producer.setClientId(ClientId);
    }

    Map<String, String> properties = producer.getProperties();
    // delivery must comfortably exceed request.timeout (retries, slow brokers).
    properties.putIfAbsent(ProducerConfig.DELIVERY_TIMEOUT_MS_CONFIG, MS_180K);
    properties.putIfAbsent(ProducerConfig.REQUEST_TIMEOUT_MS_CONFIG, MS_60K);
    properties.putIfAbsent(ProducerConfig.MAX_IN_FLIGHT_REQUESTS_PER_CONNECTION, "5");
    properties.putIfAbsent(ProducerConfig.LINGER_MS_CONFIG, "10");
    properties.putIfAbsent(ProducerConfig.ENABLE_IDEMPOTENCE_CONFIG, "true");
    properties.putIfAbsent(CommonClientConfigs.METADATA_MAX_AGE_CONFIG, MS_180K);
    properties.putIfAbsent(ProducerConfig.METADATA_MAX_IDLE_CONFIG, MS_300K);
    properties.putIfAbsent(CommonClientConfigs.CLIENT_DNS_LOOKUP_CONFIG, DNS_LOOKUP_USE_ALL_IPS);
    properties.putIfAbsent(CommonClientConfigs.RECONNECT_BACKOFF_MS_CONFIG, MS_1K);
    properties.putIfAbsent(CommonClientConfigs.RECONNECT_BACKOFF_MAX_MS_CONFIG, MS_10K);
    // Default ~10s is often too low for TLS/SASL or cross-VPC broker handshakes (e.g. MSK).
    properties.putIfAbsent(CommonClientConfigs.SOCKET_CONNECTION_SETUP_TIMEOUT_MS_CONFIG, MS_60K);
    properties.putIfAbsent(
        CommonClientConfigs.SOCKET_CONNECTION_SETUP_TIMEOUT_MAX_MS_CONFIG, MS_120K);
    properties.putIfAbsent(ProducerConfig.MAX_REQUEST_SIZE_CONFIG, "100000000");
    properties.putIfAbsent(ProducerConfig.RETRY_BACKOFF_MS_CONFIG, MS_1K);
    // Metadata / buffer acquisition during broker rolling restarts or cluster upgrades.
    properties.putIfAbsent(ProducerConfig.MAX_BLOCK_MS_CONFIG, MS_120K);
  }

  private void loadDefaultKafkaConsumerProperties(
      KafkaProperties.Consumer consumer, String clientId, String groupId) {
    consumer.setClientId(clientId);
    consumer.setGroupId(groupId);

    if (consumer.getEnableAutoCommit() == null) {
      consumer.setEnableAutoCommit(true);
    }
    if (consumer.getAutoOffsetReset() == null) {
      consumer.setAutoOffsetReset("earliest");
    }

    Map<String, String> properties = consumer.getProperties();
    properties.putIfAbsent(ConsumerConfig.MAX_POLL_RECORDS_CONFIG, "500");
    properties.putIfAbsent(ConsumerConfig.MAX_POLL_INTERVAL_MS_CONFIG, MS_300K);
    properties.putIfAbsent(ConsumerConfig.SESSION_TIMEOUT_MS_CONFIG, "45000");
    properties.putIfAbsent(ConsumerConfig.HEARTBEAT_INTERVAL_MS_CONFIG, "3000");
    properties.putIfAbsent(ConsumerConfig.FETCH_MAX_BYTES_CONFIG, "52428800");
    properties.putIfAbsent(ConsumerConfig.MAX_PARTITION_FETCH_BYTES_CONFIG, "1048576");
    properties.putIfAbsent(ConsumerConfig.RECONNECT_BACKOFF_MS_CONFIG, MS_1K);
    properties.putIfAbsent(ConsumerConfig.RECONNECT_BACKOFF_MAX_MS_CONFIG, MS_10K);
    properties.putIfAbsent(ConsumerConfig.METADATA_MAX_AGE_CONFIG, MS_180K);
    properties.putIfAbsent(ConsumerConfig.CLIENT_DNS_LOOKUP_CONFIG, DNS_LOOKUP_USE_ALL_IPS);
    properties.putIfAbsent(ConsumerConfig.REQUEST_TIMEOUT_MS_CONFIG, MS_60K);
    properties.putIfAbsent(ConsumerConfig.DEFAULT_API_TIMEOUT_MS_CONFIG, MS_120K);
    properties.putIfAbsent(ConsumerConfig.RETRY_BACKOFF_MS_CONFIG, MS_500);
    properties.putIfAbsent(CommonClientConfigs.SOCKET_CONNECTION_SETUP_TIMEOUT_MS_CONFIG, MS_60K);
    properties.putIfAbsent(
        CommonClientConfigs.SOCKET_CONNECTION_SETUP_TIMEOUT_MAX_MS_CONFIG, MS_120K);
  }

  private void loadDefaultKafkaAdminClientProperties(KafkaProperties kafkaProperties) {
    Map<String, String> properties = kafkaProperties.getAdmin().getProperties();
    properties.putIfAbsent(AdminClientConfig.REQUEST_TIMEOUT_MS_CONFIG, MS_60K);
    properties.putIfAbsent(AdminClientConfig.DEFAULT_API_TIMEOUT_MS_CONFIG, MS_120K);
    properties.putIfAbsent(AdminClientConfig.RETRY_BACKOFF_MS_CONFIG, MS_500);
    properties.putIfAbsent(CommonClientConfigs.METADATA_MAX_AGE_CONFIG, MS_180K);
    properties.putIfAbsent(CommonClientConfigs.RECONNECT_BACKOFF_MS_CONFIG, MS_1K);
    properties.putIfAbsent(CommonClientConfigs.RECONNECT_BACKOFF_MAX_MS_CONFIG, MS_10K);
    properties.putIfAbsent(CommonClientConfigs.CLIENT_DNS_LOOKUP_CONFIG, DNS_LOOKUP_USE_ALL_IPS);
    properties.putIfAbsent(CommonClientConfigs.SOCKET_CONNECTION_SETUP_TIMEOUT_MS_CONFIG, MS_60K);
    properties.putIfAbsent(
        CommonClientConfigs.SOCKET_CONNECTION_SETUP_TIMEOUT_MAX_MS_CONFIG, MS_120K);
  }

  private void loadDefaultKafkaAdminProperties(
      KafkaAdmin kafkaAdmin, KafkaProperties kafkaProperties, String clientId) {
    KafkaProperties.Admin adminProps = kafkaProperties.getAdmin();
    kafkaAdmin.setAutoCreate(adminProps.isAutoCreate());
    kafkaAdmin.setFatalIfBrokerNotAvailable(adminProps.isFailFast());
    Duration closeTimeout = adminProps.getCloseTimeout();
    if (closeTimeout != null) {
      kafkaAdmin.setCloseTimeout((int) closeTimeout.toMillis());
    }
    Duration operationTimeout = adminProps.getOperationTimeout();
    if (operationTimeout != null) {
      kafkaAdmin.setOperationTimeout((int) operationTimeout.toMillis());
    }
    kafkaAdmin.setModifyTopicConfigs(adminProps.isModifyTopicConfigs());
    if (clientId != null) {
      adminProps.setClientId(clientId);
    }
  }

  /**
   * Creates a single Kafka topic, ignoring {@link TopicExistsException}.
   *
   * @param adminClient the admin client to use
   * @param newTopic the topic definition
   */
  public static void createTopic(AdminClient adminClient, NewTopic newTopic) {
    createTopics(adminClient, List.of(newTopic));
  }

  /**
   * Creates multiple Kafka topics in batch, ignoring {@link TopicExistsException}.
   *
   * @param adminClient the admin client to use
   * @param newTopics the topic definitions to create
   */
  public static void createTopics(AdminClient adminClient, List<NewTopic> newTopics) {
    try {
      adminClient.createTopics(newTopics).all().get(90, TimeUnit.SECONDS);
    } catch (ExecutionException | InterruptedException | TimeoutException e) {
      if (e.getCause() instanceof TopicExistsException) {
        log.debug("Topic already exists: {}", newTopics);
      } else {
        throw new RuntimeException(e);
      }
    }
  }

  /**
   * Creates a topic if it does not exist, or updates its partitions and configs if it does.
   *
   * @param adminClient the admin client to use
   * @param topic the desired topic definition
   * @param existingTopics set of topic names that already exist in the cluster
   */
  public static void createOrUpdateTopic(
      AdminClient adminClient, NewTopic topic, Set<String> existingTopics) {
    String topicName = topic.name();
    try {
      int requestEdPartitions = topic.numPartitions();
      Map<String, String> requestedConfigs = topic.configs();

      if (!existingTopics.contains(topicName)) {
        adminClient.createTopics(List.of(topic)).all().get();
        return;
      }

      // UPDATE FLOW

      // 1. Check current partitions
      TopicDescription description =
          adminClient.describeTopics(List.of(topicName)).allTopicNames().get().get(topicName);

      int currentPartitions = description.partitions().size();

      // 2. Increase partitions if needed
      if (requestEdPartitions > currentPartitions) {
        adminClient
            .createPartitions(Map.of(topicName, NewPartitions.increaseTo(requestEdPartitions)))
            .all()
            .get();
      }

      // 3. Update configs ONLY IF NEEDED
      if (requestedConfigs != null && !requestedConfigs.isEmpty()) {

        ConfigResource resource = new ConfigResource(ConfigResource.Type.TOPIC, topicName);

        // Fetch current configs
        Config currentConfig =
            adminClient.describeConfigs(List.of(resource)).all().get().get(resource);

        List<AlterConfigOp> ops = new ArrayList<>();

        for (Map.Entry<String, String> entry : requestedConfigs.entrySet()) {

          String key = entry.getKey();
          String desiredValue = entry.getValue();

          ConfigEntry existingEntry = currentConfig.get(key);

          String currentValue = existingEntry != null ? existingEntry.value() : null;

          // Only update if value actually changed
          if (!Objects.equals(currentValue, desiredValue)) {
            ops.add(
                new AlterConfigOp(new ConfigEntry(key, desiredValue), AlterConfigOp.OpType.SET));
          }
        }

        // Apply only if there are changes
        if (!ops.isEmpty()) {
          adminClient.incrementalAlterConfigs(Map.of(resource, ops)).all().get();
        }
      }

    } catch (Exception e) {
      throw new RuntimeException("Failed to create or update topic: " + topicName, e);
    }
  }

  /**
   * Returns topic configuration for time- and size-based retention with delete cleanup policy.
   *
   * @param retentionTimeInMs retention period in milliseconds
   * @param retentionSizeInBytes maximum retained size in bytes per partition
   * @return map of topic config entries
   */
  public static Map<String, String> getTopicCleanUpConfigs(
      long retentionTimeInMs, long retentionSizeInBytes) {
    Map<String, String> topicConfigs = new HashMap<>();
    topicConfigs.put("retention.ms", String.valueOf(retentionTimeInMs));
    topicConfigs.put("retention.bytes", String.valueOf(retentionSizeInBytes));
    topicConfigs.put("cleanup.policy", "delete");
    return topicConfigs;
  }
}
