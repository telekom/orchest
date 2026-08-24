package io.telekom.orchest.orchestrest.configurations;

import static io.telekom.orchest.adapter.kafka.client.KafkaConstant.*;

import de.telekom.solutions.kmsclient.CryptoService;
import de.telekom.solutions.kmsclient.KMSClientBuilderService;
import io.micrometer.core.instrument.MeterRegistry;
import io.telekom.orchest.adapter.kafka.KafkaUtils;
import io.telekom.orchest.adapter.kafka.builder.KafkaBuilder;
import io.telekom.orchest.adapter.kafka.client.KafkaClient;
import io.telekom.orchest.adapter.kafka.client.KafkaEncryptionClient;
import io.telekom.orchest.adapter.kafka.config.OrchestKafkaProperties;
import io.telekom.orchest.orchestrest.event.EventProducer;
import io.telekom.orchest.telemetry.OrchestRestTelemetryService;
import lombok.RequiredArgsConstructor;
import org.apache.kafka.clients.admin.AdminClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.core.KafkaAdmin;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;

/**
 * Configures Kafka producer infrastructure (ProducerFactory, KafkaTemplate, AdminClient) for
 * OrchesT REST.
 */
@Configuration
@RequiredArgsConstructor
public class OrchestRestKafkaConfigurations {

  private final OrchestKafkaProperties kafkaProperties;
  private final KafkaBuilder<String, String> kafkaBuilder = new KafkaBuilder<>();

  @Bean(ORCHEST_ENGINE_PRODUCER_FACTORY_BEAN_NAME)
  public ProducerFactory<String, String> orchestEngineProducerFactory(MeterRegistry meterRegistry) {
    return kafkaBuilder.buildKafkaProducerFactory(
        kafkaProperties.getConfig(), "ORCHEST_REST" + KafkaUtils.getKafkaSuffix(), meterRegistry);
  }

  @Bean(ORCHEST_ENGINE_KAFKA_TEMPLATE_BEAN_NAME)
  public KafkaTemplate<String, String> orchestEngineKafkaTemplate(
      ProducerFactory<String, String> orchestEngineProducerFactory) {
    return kafkaBuilder.buildKafkaTemplate(orchestEngineProducerFactory);
  }

  @Bean(ORCHEST_ENGINE_KAFKA_ADMIN_BEAN_NAME)
  public KafkaAdmin kafkaAdmin() {
    return kafkaBuilder.buildKafkaAdmin(
        kafkaProperties.getConfig(), "ORCHEST_REST" + KafkaUtils.getKafkaSuffix());
  }

  @Bean
  public EventProducer eventProducer(
      KafkaTemplate<String, String> orchestEngineKafkaTemplate,
      KafkaEncryptionClient kafkaEncryptionClient,
      OrchestRestTelemetryService orchestRestTelemetryService) {
    return new EventProducer(
        new KafkaClient(orchestEngineKafkaTemplate, kafkaEncryptionClient),
        orchestRestTelemetryService);
  }

  @Bean(destroyMethod = "close")
  public AdminClient orchestAdminClient(KafkaAdmin kafkaAdmin) {
    return AdminClient.create(kafkaAdmin.getConfigurationProperties());
  }

  @Bean(ORCHEST_KAFKA_CRYPTO_SERVICE_BEAN_NAME)
  public CryptoService orchestCryptoService(KMSClientBuilderService kmsClientBuilderService) {
    return kmsClientBuilderService.build("kafka");
  }
}
