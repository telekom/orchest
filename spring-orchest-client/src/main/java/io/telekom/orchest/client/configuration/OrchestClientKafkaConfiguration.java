package io.telekom.orchest.client.configuration;

import static io.telekom.orchest.adapter.kafka.KafkaUtils.SEPARATOR;
import static io.telekom.orchest.adapter.kafka.client.KafkaConstant.*;

import de.telekom.solutions.kmsclient.CryptoService;
import de.telekom.solutions.kmsclient.KMSClientBuilderService;
import de.telekom.solutions.kmsclient.KMSProperties;
import io.micrometer.core.instrument.MeterRegistry;
import io.telekom.orchest.adapter.kafka.KafkaUtils;
import io.telekom.orchest.adapter.kafka.builder.KafkaBuilder;
import io.telekom.orchest.adapter.kafka.client.IEncryptionClient;
import io.telekom.orchest.adapter.kafka.client.KafkaClient;
import io.telekom.orchest.adapter.kafka.config.OrchestKafkaProperties;
import io.telekom.orchest.client.OrchestProperties;
import io.telekom.orchest.client.processor.EventProducer;
import io.telekom.orchest.telemetry.OrchestClientTelemetryService;
import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.config.TopicBuilder;
import org.springframework.kafka.core.KafkaAdmin;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;

/**
 * Configuration class for Kafka infrastructure in the OrchesT client. Sets up producer/consumer
 * factories, templates, admin clients, and required topics.
 */
@RequiredArgsConstructor
@Configuration("orchestClientKafkaConfiguration")
public class OrchestClientKafkaConfiguration {

  private final OrchestProperties orchestProperties;
  private final OrchestKafkaProperties kafkaProperties;
  private final KafkaBuilder<String, String> kafkaBuilder = new KafkaBuilder<>();
  private final ApplicationContext applicationContext;

  /**
   * Creates the Kafka listener container factory for consumers.
   *
   * @param meterRegistry The meter registry for metrics.
   * @return The concurrent listener container factory.
   */
  @Bean(ORCHEST_SPRING_CLIENT_CONSUMER_FACTORY_BEAN_NAME)
  public ConcurrentKafkaListenerContainerFactory<String, String>
      orchestSpringClientKafkaListenerContainerFactory(MeterRegistry meterRegistry) {
    return kafkaBuilder.buildConcurrentKafkaListenerContainerFactory(
        kafkaProperties.getConfig(),
        "ORCHEST_SPRING_CLIENT_" + getAppName() + KafkaUtils.getKafkaSuffix(),
        "ORCHEST_SPRING_CLIENT_" + getConsumerGroupNameSuffix(),
        meterRegistry);
  }

  /**
   * Creates the producer factory for sending messages to Kafka.
   *
   * @param meterRegistry The meter registry.
   * @return The producer factory.
   */
  @Bean(ORCHEST_SPRING_CLIENT_PRODUCER_FACTORY_BEAN_NAME)
  public ProducerFactory<String, String> orchestSpringClientProducerFactory(
      MeterRegistry meterRegistry) {
    return kafkaBuilder.buildKafkaProducerFactory(
        kafkaProperties.getConfig(),
        "ORCHEST_SPRING_CLIENT_" + getAppName() + KafkaUtils.getKafkaSuffix(),
        meterRegistry);
  }

  /**
   * Creates the Kafka template for high-level sending operations.
   *
   * @param orchestSpringClientProducerFactory The producer factory.
   * @return The Kafka template.
   */
  @Bean(ORCHEST_SPRING_CLIENT_KAFKA_TEMPLATE_BEAN_NAME)
  public KafkaTemplate<String, String> orchestSpringClientKafkaTemplate(
      @Qualifier(ORCHEST_SPRING_CLIENT_PRODUCER_FACTORY_BEAN_NAME)
          ProducerFactory<String, String> orchestSpringClientProducerFactory) {
    return kafkaBuilder.buildKafkaTemplate(orchestSpringClientProducerFactory);
  }

  /**
   * Creates the Kafka Admin client for managing topics.
   *
   * @return The Kafka Admin client.
   */
  @Bean(ORCHEST_SPRING_CLIENT_KAFKA_ADMIN_BEAN_NAME)
  public KafkaAdmin orchestSpringClientKafkaAdmin() {
    return kafkaBuilder.buildKafkaAdmin(
        kafkaProperties.getConfig(),
        "ORCHEST_SPRING_CLIENT_" + getAppName() + KafkaUtils.getKafkaSuffix());
  }

  /**
   * Creates the event producer service for sending engine events.
   *
   * @param orchestSpringClientKafkaTemplate The Kafka template.
   * @param encryptionClient The encryption client.
   * @param orchestClientTelemetryService The telemetry service.
   * @return The event producer.
   */
  @Bean
  public EventProducer eventProducer(
      @Qualifier(ORCHEST_SPRING_CLIENT_KAFKA_TEMPLATE_BEAN_NAME)
          KafkaTemplate<String, String> orchestSpringClientKafkaTemplate,
      IEncryptionClient encryptionClient,
      OrchestClientTelemetryService orchestClientTelemetryService) {
    return new EventProducer(
        new KafkaClient(orchestSpringClientKafkaTemplate, encryptionClient),
        orchestClientTelemetryService);
  }

  /**
   * Auto-creates Kafka topics for all configured worker process IDs.
   *
   * @return the topic definitions
   */
  @Bean
  public KafkaAdmin.NewTopics workerConsumerTopics() {
    List<NewTopic> topics = new ArrayList<>();
    orchestProperties
        .getProcessIds()
        .forEach(
            processId -> {
              topics.add(
                  TopicBuilder.name(KafkaUtils.getClientWorkerEventTopic(processId))
                      .partitions(10)
                      .replicas(getReplicationCount())
                      .configs(
                          KafkaBuilder.getTopicCleanUpConfigs(
                              Duration.of(5, ChronoUnit.DAYS).toMillis(), 5368709120L))
                      .build());
            });
    return new KafkaAdmin.NewTopics(topics.toArray(new NewTopic[0]));
  }

  /**
   * Builds the KMS-backed crypto service for Kafka payload encryption.
   *
   * @param orchestProperties the OrchesT configuration properties
   * @return the configured crypto service
   */
  @Bean(ORCHEST_KAFKA_CRYPTO_SERVICE_BEAN_NAME)
  public CryptoService kafkaEncryptionService(OrchestProperties orchestProperties) {
    OrchestProperties.Encryption encryption = orchestProperties.getEncryption();
    KMSProperties.AWS aws = new KMSProperties.AWS();
    aws.setIamRoleARN(encryption.getIamRoleARN());

    KMSProperties.DataKeyCache dataKeyCache = new KMSProperties.DataKeyCache();
    dataKeyCache.setCapacity(200);
    dataKeyCache.setMaxAge(60000); // in seconds
    dataKeyCache.setMaxUsageLimit(10000000);

    KMSProperties.KMSClientProperties kmsClientProperties = new KMSProperties.KMSClientProperties();
    kmsClientProperties.setEnabled(encryption.isEnableEncryption());
    kmsClientProperties.setAws(aws);
    kmsClientProperties.setDataKeyCaching(dataKeyCache);
    kmsClientProperties.setKmsKeyARN(encryption.getKmsKeyARN());

    return KMSClientBuilderService.build(kmsClientProperties, "test", "");
  }

  /**
   * Returns the topic replication factor based on the environment (1 for LOCAL/TEST, 3 otherwise).
   *
   * @return the replication count
   */
  public int getReplicationCount() {
    String environment = KafkaUtils.ENVIRONMENT;
    return environment.equals("LOCAL") || environment.equals("TEST") ? 1 : 3;
  }

  private String getConsumerGroupNameSuffix() {
    return getAppName() + SEPARATOR + "CONSUMER" + KafkaUtils.getKafkaSuffix();
  }

  /**
   * Derives the normalized application name used in Kafka client/group IDs.
   *
   * @return the uppercased, separator-normalized application name
   */
  public String getAppName() {
    String appName =
        applicationContext.getId().equalsIgnoreCase("application")
            ? applicationContext.getApplicationName()
            : applicationContext.getId();
    return appName.replace("-", SEPARATOR).replace("/", "").toUpperCase();
  }
}
