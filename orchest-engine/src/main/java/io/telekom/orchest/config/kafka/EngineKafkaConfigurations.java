package io.telekom.orchest.config.kafka;

import static io.telekom.orchest.adapter.kafka.client.KafkaConstant.*;
import static io.telekom.orchest.adapter.kafka.client.TopicConstant.*;

import de.telekom.solutions.kmsclient.CryptoService;
import de.telekom.solutions.kmsclient.KMSClientBuilderService;
import io.micrometer.core.instrument.MeterRegistry;
import io.telekom.orchest.adapter.kafka.KafkaClientEventProducer;
import io.telekom.orchest.adapter.kafka.KafkaUtils;
import io.telekom.orchest.adapter.kafka.builder.KafkaBuilder;
import io.telekom.orchest.adapter.kafka.client.KafkaClient;
import io.telekom.orchest.adapter.kafka.client.KafkaEncryptionClient;
import io.telekom.orchest.adapter.kafka.config.OrchestKafkaProperties;
import io.telekom.orchest.api.core.properties.EngineScale;
import io.telekom.orchest.config.engine.EngineProperties;
import io.telekom.orchest.telemetry.OrchestEngineTelemetryService;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Stream;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.admin.AdminClient;
import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.DependsOn;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.config.TopicBuilder;
import org.springframework.kafka.core.KafkaAdmin;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;

/**
 * Configuration for Kafka infrastructure in the Engine module. Sets up Kafka producers, consumers,
 * and admin clients specifically for the engine's needs. Also handles topic creation based on the
 * configured {@link EngineScale}.
 */
@Slf4j
@Configuration
@RequiredArgsConstructor
public class EngineKafkaConfigurations {

  private final OrchestKafkaProperties orchestKafkaProperties;
  private final EngineProperties engineProperties;
  private final KafkaBuilder<String, String> kafkaBuilder = new KafkaBuilder<>();

  /**
   * Configures the Kafka listener container factory for engine consumers.
   *
   * @param meterRegistry Metric registry.
   * @return ConcurrentKafkaListenerContainerFactory.
   */
  @DependsOn("engineTopics")
  @Bean(ORCHEST_ENGINE_CONSUMER_FACTORY_BEAN_NAME)
  public ConcurrentKafkaListenerContainerFactory<String, String>
      orchestEngineKafkaListenerContainerFactory(MeterRegistry meterRegistry) {
    return kafkaBuilder.buildConcurrentKafkaListenerContainerFactory(
        orchestKafkaProperties.getConfig(),
        "ORCHEST_ENGINE" + KafkaUtils.getKafkaSuffix(),
        "ORCHEST_ENGINE_CONSUMER" + KafkaUtils.getKafkaSuffix(),
        meterRegistry);
  }

  /**
   * Configures the Kafka producer factory for the engine.
   *
   * @param meterRegistry Metric registry.
   * @return ProducerFactory.
   */
  @Bean(ORCHEST_ENGINE_PRODUCER_FACTORY_BEAN_NAME)
  public ProducerFactory<String, String> orchestEngineProducerFactory(MeterRegistry meterRegistry) {
    return kafkaBuilder.buildKafkaProducerFactory(
        orchestKafkaProperties.getConfig(),
        "ORCHEST_ENGINE" + KafkaUtils.getKafkaSuffix(),
        meterRegistry);
  }

  /**
   * Configures the Kafka template for the engine.
   *
   * @param orchestEngineProducerFactory The producer factory.
   * @return KafkaTemplate.
   */
  @Bean(ORCHEST_ENGINE_KAFKA_TEMPLATE_BEAN_NAME)
  public KafkaTemplate<String, String> orchestEngineKafkaTemplate(
      ProducerFactory<String, String> orchestEngineProducerFactory) {
    return kafkaBuilder.buildKafkaTemplate(orchestEngineProducerFactory);
  }

  /**
   * Configures the Kafka Admin client.
   *
   * @return KafkaAdmin.
   */
  @Bean(ORCHEST_ENGINE_KAFKA_ADMIN_BEAN_NAME)
  public KafkaAdmin kafkaAdmin() {
    return kafkaBuilder.buildKafkaAdmin(
        orchestKafkaProperties.getConfig(), "ORCHEST_ENGINE" + KafkaUtils.getKafkaSuffix());
  }

  /**
   * Configures the event producer for sending events from the engine.
   *
   * @param orchestEngineKafkaTemplate The Kafka template.
   * @param kafkaEncryptionClient The encryption client.
   * @return KafkaClientEventProducer.
   */
  @Bean
  public KafkaClientEventProducer eventProducer(
      KafkaTemplate<String, String> orchestEngineKafkaTemplate,
      KafkaEncryptionClient kafkaEncryptionClient,
      OrchestEngineTelemetryService telemetryService) {
    return new KafkaClientEventProducer(
        new KafkaClient(orchestEngineKafkaTemplate, kafkaEncryptionClient), telemetryService);
  }

  /**
   * Configures the KMS crypto service for Kafka encryption.
   *
   * @param kmsClientBuilderService The KMS builder service.
   * @return CryptoService.
   */
  @Bean(ORCHEST_KAFKA_CRYPTO_SERVICE_BEAN_NAME)
  public CryptoService orchestCryptoService(KMSClientBuilderService kmsClientBuilderService) {
    return kmsClientBuilderService.build("kafka");
  }

  /**
   * Registers required Kafka topics for the engine. Uses {@link AdminClient} to create topics if
   * they don't exist.
   *
   * @param kafkaAdmin The Kafka Admin client.
   * @return List of created NewTopic objects.
   */
  @Bean
  public List<NewTopic> engineTopics(KafkaAdmin kafkaAdmin) {
    List<NewTopic> engineTopics = getAllEngineTopics();
    try (AdminClient adminClient = AdminClient.create(kafkaAdmin.getConfigurationProperties())) {
      KafkaBuilder.createTopics(adminClient, engineTopics);
    } catch (Exception e) {
      log.error("failed to create engine topics", e);
    }
    return engineTopics;
  }

  /**
   * Defines the list of topics required by the engine, with partition counts based on engine scale.
   *
   * @return List of NewTopic definitions.
   */
  public List<NewTopic> getAllEngineTopics() {
    EngineScale engineScale = engineProperties.getEngineScale();
    int replicationCount = engineScale.getReplicationCount();
    Map<String, String> topicConfigs =
        KafkaBuilder.getTopicCleanUpConfigs(
            engineProperties.getTopicDataRetention().toMillis(),
            engineProperties.getTopicDataRetentionBytes());

    // Low usage topics
    Stream<NewTopic> lowUsageTopics =
        Stream.of(
                KafkaUtils.getTopicWithEnvSuffix(DEPLOYMENT_TOPIC),
                KafkaUtils.getTopicWithEnvSuffix(CLIENT_WORKER_REGISTER_EVENT_TOPIC),
                KafkaUtils.getTopicWithEnvSuffix(SERVER_PENDING_TASK_EVENT_TOPIC),
                KafkaUtils.getTopicWithEnvSuffix(SERVER_INCIDENT_EVENT),
                KafkaUtils.getTopicWithEnvSuffix(SERVER_INCIDENT_RESOLUTION_EVENT))
            .map(
                topic ->
                    TopicBuilder.name(topic)
                        .partitions(engineScale.getDefaultPartitionCount())
                        .configs(topicConfigs)
                        .replicas(replicationCount)
                        .build());

    // Medium usage topics
    Stream<NewTopic> mediumUsageTopics =
        Stream.of(
                KafkaUtils.getTopicWithEnvSuffix(SERVER_INTERMEDIATE_MESSAGE_THROW_EVENT_TOPIC),
                KafkaUtils.getTopicWithEnvSuffix(SERVER_INTERMEDIATE_SIGNAL_THROW_EVENT_TOPIC),
                KafkaUtils.getTopicWithEnvSuffix(SERVER_WORKER_EVENT_TOPIC),
                KafkaUtils.getTopicWithEnvSuffix(PROCESS_RESUME_EVENT_TOPIC),
                KafkaUtils.getTopicWithEnvSuffix(SERVER_RETRY_PROCESS_EVENT_TOPIC))
            .map(
                topic ->
                    TopicBuilder.name(topic)
                        .partitions(engineScale.getFactoredPartitionCount())
                        .configs(topicConfigs)
                        .replicas(replicationCount)
                        .build());

    // High usage topics
    Stream<NewTopic> highUsageTopics =
        Stream.of(
                KafkaUtils.getTopicWithEnvSuffix(PROCESS_INVOCATION_EVENT_TOPIC),
                KafkaUtils.getTopicWithEnvSuffix(CLIENT_COMMON_WORKER_EVENT_TOPIC),
                KafkaUtils.getTopicWithEnvSuffix(DYNAMIC_PROCESS_INVOCATION_EVENT_TOPIC))
            .map(
                topic ->
                    TopicBuilder.name(topic)
                        .partitions(engineScale.getPartitionCount())
                        .configs(topicConfigs)
                        .replicas(replicationCount)
                        .build());

    return Stream.of(lowUsageTopics, mediumUsageTopics, highUsageTopics)
        .flatMap(Function.identity())
        .toList();
  }
}
