package io.telekom.orchest.connectorservice.config.kafka;

import static io.telekom.orchest.adapter.kafka.KafkaUtils.getConnectorTaskTopic;
import static io.telekom.orchest.adapter.kafka.KafkaUtils.getGroupIdWithEnvSuffix;
import static io.telekom.orchest.adapter.kafka.client.KafkaConstant.ORCHEST_ENGINE_CONSUMER_FACTORY_BEAN_NAME;
import static io.telekom.orchest.adapter.kafka.client.TopicConstant.CONNECTOR_TASK_EVENT_TOPIC;

import io.telekom.orchest.adapter.kafka.DynamicKafkaConsumerFactory;
import io.telekom.orchest.adapter.kafka.client.IEncryptionClient;
import io.telekom.orchest.api.core.adapters.data.dto.ConnectorTaskRequest;
import io.telekom.orchest.api.core.properties.EngineScale;
import io.telekom.orchest.connectorservice.config.engine.EngineProperties;
import io.telekom.orchest.connectorservice.service.ConnectorExecutionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.DependsOn;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;

/** Registers the Kafka consumer for connector task events. */
@Slf4j
@Configuration
@DependsOn({"connectorKafkaConfigurations", "kafkaUtils"})
@RequiredArgsConstructor
public class ConnectorConsumerRegisterConfig {

  private final IEncryptionClient encryptionClient;
  private final EngineProperties engineProperties;

  /**
   * Creates and registers the dynamic Kafka consumer that consumes connector task events from the
   * connector task topic and delegates them to the {@link ConnectorExecutionService}.
   *
   * @param factory the Kafka listener container factory
   * @param connectorExecutionService the service that executes connector tasks
   * @return a configured DynamicKafkaConsumerFactory for connector task requests
   */
  @Bean
  public DynamicKafkaConsumerFactory<ConnectorTaskRequest> connectorTaskEventConsumer(
      @Qualifier(ORCHEST_ENGINE_CONSUMER_FACTORY_BEAN_NAME)
          ConcurrentKafkaListenerContainerFactory<String, String> factory,
      ConnectorExecutionService connectorExecutionService) {
    EngineScale scale = engineProperties.getEngineScale();
    int concurrency =
        EngineScale.getConcurrency(
            scale.getFactoredPartitionCount(), engineProperties.getBrokerCount());
    log.info("concurrency for connectorTaskEventConsumer/pod: {}", concurrency);
    DynamicKafkaConsumerFactory<ConnectorTaskRequest> consumer =
        new DynamicKafkaConsumerFactory<>(
            factory,
            connectorExecutionService::execute,
            encryptionClient,
            ConnectorTaskRequest.class);
    consumer.registerWorkerConsumer(
        getConnectorTaskTopic(), getGroupIdWithEnvSuffix(CONNECTOR_TASK_EVENT_TOPIC), concurrency);
    return consumer;
  }
}
