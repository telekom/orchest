package io.telekom.orchest.adapter.kafka;

import io.telekom.orchest.adapter.kafka.builder.KafkaBuilder;
import io.telekom.orchest.adapter.kafka.client.IEncryptionClient;
import io.telekom.orchest.adapter.kafka.config.WorkerConsumerConfig;
import io.telekom.orchest.api.core.adapters.data.dto.WorkerEventRequest;
import io.telekom.orchest.api.core.properties.EngineScale;
import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.function.Consumer;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.admin.AdminClient;
import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.context.SmartLifecycle;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.config.TopicBuilder;
import org.springframework.kafka.core.KafkaAdmin;

/**
 * Loads and registers Kafka consumers for WorkerEventRequest from consumer.json configuration. Each
 * consumer uses {@link io.telekom.orchest.enginecore.bpmn.eventhandler.IWorkerEventEventHandler} as
 * the handler.
 *
 * <p>Can be triggered from any module by injecting this bean and calling {@link #loadConsumers()}.
 * Consumers are automatically registered on context startup when the event handler is available.
 * Implements {@link SmartLifecycle} for graceful shutdown.
 *
 * <p>Consumer loading runs asynchronously to avoid blocking application startup and health probes.
 */
@Slf4j
public class WorkerEventConsumerLoader implements SmartLifecycle {

  private final List<WorkerConsumerConfig> workerConsumerConfigs;
  private final KafkaAdmin kafkaAdmin;
  private EngineScale engineScale;

  private DynamicKafkaConsumerFactory<WorkerEventRequest> consumerFactory;
  private volatile boolean running = false;
  private volatile boolean consumersLoaded = false;

  /**
   * Constructs the loader with the required Kafka infrastructure and configuration.
   *
   * @param kafkaListenerContainerFactory factory for creating listener containers
   * @param encryptionClient handles message encryption/decryption
   * @param workerConsumerConfigs list of worker consumer configurations to register
   * @param consumer the event handler that processes incoming worker events
   * @param engineScale scaling properties for topic partitions and replicas
   * @param kafkaAdmin admin client for topic management
   */
  public WorkerEventConsumerLoader(
      ConcurrentKafkaListenerContainerFactory<String, String> kafkaListenerContainerFactory,
      IEncryptionClient encryptionClient,
      List<WorkerConsumerConfig> workerConsumerConfigs,
      Consumer<WorkerEventRequest> consumer,
      EngineScale engineScale,
      KafkaAdmin kafkaAdmin) {
    this.engineScale = engineScale;
    this.workerConsumerConfigs = workerConsumerConfigs;
    this.kafkaAdmin = kafkaAdmin;
    this.consumerFactory =
        new DynamicKafkaConsumerFactory<>(
            kafkaListenerContainerFactory, consumer, encryptionClient, WorkerEventRequest.class);
  }

  /**
   * Loads consumers from the specified config resource and registers them. Uses a single
   * AdminClient to verify/create all topics in batch, then registers all consumers.
   *
   * @return true if consumers were loaded successfully, false if handler is not available or config
   *     is empty.
   */
  public boolean loadConsumers() {
    if (consumersLoaded) {
      log.warn("Consumers already loaded - skipping duplicate registration");
      return true;
    }

    if (workerConsumerConfigs.isEmpty()) {
      log.warn("No consumer configs found!!!");
      return false;
    }
    workerConsumerConfigs.forEach(this::createConsumer);
    consumerFactory.start();
    log.info(
        "started the worker event consumer - total {} consumers registered :{}",
        workerConsumerConfigs.size(),
        workerConsumerConfigs.stream().map(WorkerConsumerConfig::getTopic));
    consumersLoaded = true;
    return true;
  }

  /**
   * Creates and registers a single Kafka consumer from the given configuration.
   *
   * @param config the consumer configuration specifying topic, group, and concurrency
   */
  public void createConsumer(WorkerConsumerConfig config) {
    if (config == null) {
      log.warn("consumer config is null");
      return;
    }

    String topic = config.getTopic();
    String groupId = config.getGroupId();
    int concurrency = config.getConcurrency();
    consumerFactory.registerWorkerConsumer(topic, groupId, concurrency);
    log.info("loaded process worker event consumer on topic {} ", topic);
  }

  /**
   * Stops the consumer for the specified topic.
   *
   * @param topic the topic whose consumer should be stopped
   */
  public void stopConsumer(String topic) {
    consumerFactory.stopConsumer(topic);
  }

  /**
   * Checks whether a consumer is currently active for the given topic.
   *
   * @param topic the topic to check
   * @return true if a consumer is running for the topic
   */
  public boolean isConsumerRunning(String topic) {
    return consumerFactory.isConsumerRunning(topic);
  }

  /**
   * Verifies all required topics exist using a single AdminClient connection, then batch-creates
   * any missing topics.
   */
  public void ensureAllTopicsExist(List<WorkerConsumerConfig> configs) {
    log.info("ensuring server worker event topics");

    try (AdminClient adminClient = AdminClient.create(kafkaAdmin.getConfigurationProperties())) {
      List<NewTopic> engineWorkerEventTopic =
          configs.stream()
              .map(WorkerConsumerConfig::getTopic)
              .map(
                  topic ->
                      TopicBuilder.name(topic)
                          .partitions(engineScale.getDefaultPartitionCount())
                          .replicas(engineScale.getReplicationCount())
                          .configs(
                              KafkaBuilder.getTopicCleanUpConfigs(
                                  Duration.of(5, ChronoUnit.DAYS).toMillis(), 5368709120L))
                          .build())
              .toList();

      KafkaBuilder.createTopics(adminClient, engineWorkerEventTopic);
      log.info("ensured {} server worker event topics", configs.size());
    } catch (Exception e) {
      log.error("Failed to ensure topics exist", e);
    }
  }

  @Override
  public void start() {
    running = true;
    loadConsumers();
  }

  @Override
  public void stop() {
    log.info("Shutting down configured worker event consumers..");
    if (consumerFactory != null) {
      consumerFactory.stop();
      consumerFactory = null;
    }
    consumersLoaded = false;
    running = false;
  }

  @Override
  public boolean isRunning() {
    return running;
  }

  @Override
  public int getPhase() {
    return Integer.MAX_VALUE; // Stop late in the shutdown process
  }
}
