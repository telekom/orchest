package io.telekom.orchest.configuration.kafka;

import static io.telekom.orchest.adapter.kafka.client.KafkaConstant.*;

import de.telekom.solutions.kmsclient.CryptoService;
import de.telekom.solutions.kmsclient.KMSClientBuilderService;
import io.micrometer.core.instrument.MeterRegistry;
import io.telekom.orchest.adapter.kafka.KafkaClientEventProducer;
import io.telekom.orchest.adapter.kafka.KafkaUtils;
import io.telekom.orchest.adapter.kafka.builder.KafkaBuilder;
import io.telekom.orchest.adapter.kafka.client.KafkaClient;
import io.telekom.orchest.adapter.kafka.client.KafkaEncryptionClient;
import io.telekom.orchest.adapter.kafka.config.OrchestKafkaProperties;
import io.telekom.orchest.telemetry.OrchestEngineTelemetryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.core.KafkaAdmin;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;

/**
 * Kafka configuration for the Sentinel application. Configures producer/admin clients and
 * encryption services required for background tasks like sending alerts or processing events.
 */
@Slf4j
@Configuration
@RequiredArgsConstructor
public class SentinelKafkaConfigurations {

  private final OrchestKafkaProperties orchestKafkaProperties;
  private final KafkaBuilder<String, String> kafkaBuilder = new KafkaBuilder<>();

  /**
   * Creates the Kafka producer factory.
   *
   * @param meterRegistry Metric registry.
   * @return The ProducerFactory.
   */
  @Bean(ORCHEST_ENGINE_PRODUCER_FACTORY_BEAN_NAME)
  public ProducerFactory<String, String> orchestEngineProducerFactory(MeterRegistry meterRegistry) {
    return kafkaBuilder.buildKafkaProducerFactory(
        orchestKafkaProperties.getConfig(),
        "ORCHEST_SENTINEL" + KafkaUtils.getKafkaSuffix(),
        meterRegistry);
  }

  /**
   * Creates the Kafka template.
   *
   * @param orchestEngineProducerFactory The producer factory.
   * @return The KafkaTemplate.
   */
  @Bean(ORCHEST_ENGINE_KAFKA_TEMPLATE_BEAN_NAME)
  public KafkaTemplate<String, String> orchestEngineKafkaTemplate(
      ProducerFactory<String, String> orchestEngineProducerFactory) {
    return kafkaBuilder.buildKafkaTemplate(orchestEngineProducerFactory);
  }

  /**
   * Creates the Kafka Admin client.
   *
   * @return The KafkaAdmin.
   */
  @Bean(ORCHEST_ENGINE_KAFKA_ADMIN_BEAN_NAME)
  public KafkaAdmin kafkaAdmin() {
    return kafkaBuilder.buildKafkaAdmin(
        orchestKafkaProperties.getConfig(), "ORCHEST_SENTINEL" + KafkaUtils.getKafkaSuffix());
  }

  /**
   * Creates the event producer.
   *
   * @param orchestEngineKafkaTemplate The Kafka template.
   * @param kafkaEncryptionClient The encryption client.
   * @return The KafkaClientEventProducer.
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
   * @return The CryptoService.
   */
  @Bean(ORCHEST_KAFKA_CRYPTO_SERVICE_BEAN_NAME)
  public CryptoService orchestCryptoService(KMSClientBuilderService kmsClientBuilderService) {
    return kmsClientBuilderService.build("kafka");
  }
}
