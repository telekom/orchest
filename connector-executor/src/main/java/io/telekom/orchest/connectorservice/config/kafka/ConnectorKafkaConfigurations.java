package io.telekom.orchest.connectorservice.config.kafka;

import static io.telekom.orchest.adapter.kafka.client.KafkaConstant.*;
import static io.telekom.orchest.adapter.kafka.client.TopicConstant.CONNECTOR_TASK_EVENT_TOPIC;

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
import io.telekom.orchest.connectorservice.config.engine.EngineProperties;
import io.telekom.orchest.telemetry.OrchestEngineTelemetryService;
import java.util.List;
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
 * Kafka infrastructure for the connector service. Configures the producer (used to resume workers /
 * re-dispatch connectors / publish incidents), the listener container factory (used to consume
 * connector task events) and the connector topic.
 */
@Slf4j
@Configuration
@RequiredArgsConstructor
public class ConnectorKafkaConfigurations {

  private static final String CLIENT_ID = "ORCHEST_CONNECTOR_SERVICE";

  private final OrchestKafkaProperties orchestKafkaProperties;
  private final EngineProperties engineProperties;
  private final KafkaBuilder<String, String> kafkaBuilder = new KafkaBuilder<>();

  /**
   * Builds the Kafka listener container factory for consuming connector task events.
   *
   * @param meterRegistry the meter registry for observability
   * @return a configured ConcurrentKafkaListenerContainerFactory
   */
  @DependsOn("connectorTopics")
  @Bean(ORCHEST_ENGINE_CONSUMER_FACTORY_BEAN_NAME)
  public ConcurrentKafkaListenerContainerFactory<String, String>
      orchestConnectorKafkaListenerContainerFactory(MeterRegistry meterRegistry) {
    return kafkaBuilder.buildConcurrentKafkaListenerContainerFactory(
        orchestKafkaProperties.getConfig(),
        CLIENT_ID + KafkaUtils.getKafkaSuffix(),
        CLIENT_ID + "_CONSUMER" + KafkaUtils.getKafkaSuffix(),
        meterRegistry);
  }

  /**
   * Builds the Kafka producer factory used by the connector service to publish events.
   *
   * @param meterRegistry the meter registry for observability
   * @return a configured ProducerFactory
   */
  @Bean(ORCHEST_ENGINE_PRODUCER_FACTORY_BEAN_NAME)
  public ProducerFactory<String, String> orchestConnectorProducerFactory(
      MeterRegistry meterRegistry) {
    return kafkaBuilder.buildKafkaProducerFactory(
        orchestKafkaProperties.getConfig(), CLIENT_ID + KafkaUtils.getKafkaSuffix(), meterRegistry);
  }

  /**
   * Creates the KafkaTemplate for sending messages from the connector service.
   *
   * @param orchestConnectorProducerFactory the producer factory
   * @return a configured KafkaTemplate
   */
  @Bean(ORCHEST_ENGINE_KAFKA_TEMPLATE_BEAN_NAME)
  public KafkaTemplate<String, String> orchestConnectorKafkaTemplate(
      ProducerFactory<String, String> orchestConnectorProducerFactory) {
    return kafkaBuilder.buildKafkaTemplate(orchestConnectorProducerFactory);
  }

  /**
   * Creates the KafkaAdmin for topic management operations.
   *
   * @return a configured KafkaAdmin
   */
  @Bean(ORCHEST_ENGINE_KAFKA_ADMIN_BEAN_NAME)
  public KafkaAdmin kafkaAdmin() {
    return kafkaBuilder.buildKafkaAdmin(
        orchestKafkaProperties.getConfig(), CLIENT_ID + KafkaUtils.getKafkaSuffix());
  }

  /**
   * Creates the event producer used to publish worker, incident, and connector events.
   *
   * @param orchestConnectorKafkaTemplate the Kafka template
   * @param kafkaEncryptionClient the encryption client for message-level encryption
   * @param telemetryService the telemetry service
   * @return a configured KafkaClientEventProducer
   */
  @Bean
  public KafkaClientEventProducer eventProducer(
      KafkaTemplate<String, String> orchestConnectorKafkaTemplate,
      KafkaEncryptionClient kafkaEncryptionClient,
      OrchestEngineTelemetryService telemetryService) {
    return new KafkaClientEventProducer(
        new KafkaClient(orchestConnectorKafkaTemplate, kafkaEncryptionClient), telemetryService);
  }

  /**
   * Builds the KMS-backed crypto service used for Kafka message encryption/decryption.
   *
   * @param kmsClientBuilderService the KMS client builder
   * @return a CryptoService configured for the "kafka" key alias
   */
  @Bean(ORCHEST_KAFKA_CRYPTO_SERVICE_BEAN_NAME)
  public CryptoService orchestCryptoService(KMSClientBuilderService kmsClientBuilderService) {
    return kmsClientBuilderService.build("kafka");
  }

  /**
   * Ensures the connector task topic exists.
   *
   * @param kafkaAdmin The Kafka Admin client.
   * @return List of created NewTopic objects.
   */
  @Bean
  public List<NewTopic> connectorTopics(KafkaAdmin kafkaAdmin) {
    EngineScale engineScale = engineProperties.getEngineScale();
    NewTopic connectorTopic =
        TopicBuilder.name(KafkaUtils.getTopicWithEnvSuffix(CONNECTOR_TASK_EVENT_TOPIC))
            .partitions(engineScale.getFactoredPartitionCount())
            .configs(
                KafkaBuilder.getTopicCleanUpConfigs(
                    engineProperties.getTopicDataRetention().toMillis(),
                    engineProperties.getTopicDataRetentionBytes()))
            .replicas(engineScale.getReplicationCount())
            .build();
    List<NewTopic> topics = List.of(connectorTopic);
    try (AdminClient adminClient = AdminClient.create(kafkaAdmin.getConfigurationProperties())) {
      KafkaBuilder.createTopics(adminClient, topics);
    } catch (Exception e) {
      log.error("failed to create connector topics", e);
    }
    return topics;
  }
}
