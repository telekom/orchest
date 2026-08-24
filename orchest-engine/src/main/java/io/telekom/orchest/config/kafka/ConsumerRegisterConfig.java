package io.telekom.orchest.config.kafka;

import static io.telekom.orchest.adapter.kafka.KafkaUtils.*;
import static io.telekom.orchest.adapter.kafka.KafkaUtils.getGroupIdWithEnvSuffix;
import static io.telekom.orchest.adapter.kafka.client.KafkaConstant.*;
import static io.telekom.orchest.adapter.kafka.client.TopicConstant.*;
import static io.telekom.orchest.adapter.mongo.config.ProcessDefinitionChangeEvent.OperationType.INSERT;

import io.telekom.orchest.adapter.kafka.DynamicKafkaConsumerFactory;
import io.telekom.orchest.adapter.kafka.KafkaUtils;
import io.telekom.orchest.adapter.kafka.WorkerEventConsumerLoader;
import io.telekom.orchest.adapter.kafka.client.IEncryptionClient;
import io.telekom.orchest.adapter.kafka.config.WorkerConsumerConfig;
import io.telekom.orchest.adapter.mongo.config.ProcessDefinitionChangeEvent;
import io.telekom.orchest.api.core.adapters.data.dto.ResumeActivityEventRequest;
import io.telekom.orchest.api.core.adapters.data.dto.WorkerEventRequest;
import io.telekom.orchest.api.core.adapters.data.model.ProcessDefinition;
import io.telekom.orchest.api.core.adapters.data.repository.ProcessDefinitionRepository;
import io.telekom.orchest.api.core.properties.EngineScale;
import io.telekom.orchest.api.core.request.*;
import io.telekom.orchest.config.engine.EngineProperties;
import io.telekom.orchest.enginecore.bpmn.eventhandler.*;
import java.util.List;
import java.util.function.Consumer;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.DependsOn;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.KafkaAdmin;

/**
 * Configuration class for registering dynamic Kafka consumers. Sets up consumers for various system
 * events based on the engine configuration and scale properties.
 */
@Slf4j
@Configuration
@DependsOn(value = {"engineKafkaConfigurations", "kafkaUtils"})
@RequiredArgsConstructor
public class ConsumerRegisterConfig {

  private final IEncryptionClient encryptionClient;
  private final EngineProperties engineProperties;
  private final ApplicationEventPublisher applicationEventPublisher;

  /**
   * Creates the consumer for resource deployment events.
   *
   * @param factory the Kafka listener container factory
   * @param handler the deployment event handler
   * @return the configured consumer factory
   */
  @Bean
  public DynamicKafkaConsumerFactory<ResourceDeploymentRequest> deploymentEventConsumer(
      @Qualifier(ORCHEST_ENGINE_CONSUMER_FACTORY_BEAN_NAME)
          ConcurrentKafkaListenerContainerFactory<String, String> factory,
      EventHandler<ResourceDeploymentRequest, Object> handler) {
    int concurrency = getConcurrency(scale().getDefaultPartitionCount());
    log.info("concurrency for deploymentEventConsumer/pod: {}", concurrency);
    return register(
        factory,
        resourceDeploymentRequest ->
            handler.handle(
                resourceDeploymentRequest,
                (a, processDefinition) -> {
                  if (processDefinition instanceof ProcessDefinition pd) {
                    // post-processing to update cache or do any post-processing on new deployment
                    applicationEventPublisher.publishEvent(
                        new ProcessDefinitionChangeEvent(this, pd, INSERT));
                  }
                }),
        ResourceDeploymentRequest.class,
        DEPLOYMENT_TOPIC,
        concurrency);
  }

  /**
   * Creates the consumer for resume activity events.
   *
   * @param factory the Kafka listener container factory
   * @param handler the resume activity event handler
   * @return the configured consumer factory
   */
  @Bean
  public DynamicKafkaConsumerFactory<ResumeActivityEventRequest> resumeActivityEventConsumer(
      @Qualifier(ORCHEST_ENGINE_CONSUMER_FACTORY_BEAN_NAME)
          ConcurrentKafkaListenerContainerFactory<String, String> factory,
      EventHandler<ResumeActivityEventRequest, Void> handler) {
    int concurrency = getConcurrency(scale().getDefaultPartitionCount());
    log.info("concurrency for resumeActivityEventConsumer/pod: {}", concurrency);
    return register(
        factory,
        handler::handle,
        ResumeActivityEventRequest.class,
        PROCESS_RESUME_EVENT_TOPIC,
        concurrency);
  }

  /**
   * Creates the consumer for process invocation events.
   *
   * @param factory the Kafka listener container factory
   * @param handler the process invocation event handler
   * @return the configured consumer factory
   */
  @Bean
  public DynamicKafkaConsumerFactory<ProcessInvocationRequest> processInstanceEventConsumer(
      @Qualifier(ORCHEST_ENGINE_CONSUMER_FACTORY_BEAN_NAME)
          ConcurrentKafkaListenerContainerFactory<String, String> factory,
      EventHandler<ProcessInvocationRequest, Void> handler) {
    int concurrency = getConcurrency(scale().getPartitionCount());
    log.info("concurrency for processInstanceEventConsumer/pod: {}", concurrency);
    return register(
        factory,
        handler::handle,
        ProcessInvocationRequest.class,
        PROCESS_INVOCATION_EVENT_TOPIC,
        concurrency);
  }

  /**
   * Creates the consumer for message intermediate throw events.
   *
   * @param factory the Kafka listener container factory
   * @param handler the message event handler
   * @return the configured consumer factory
   */
  @Bean
  public DynamicKafkaConsumerFactory<MessageEventRequest> messageIntermediateThrowEventConsumer(
      @Qualifier(ORCHEST_ENGINE_CONSUMER_FACTORY_BEAN_NAME)
          ConcurrentKafkaListenerContainerFactory<String, String> factory,
      EventHandler<MessageEventRequest, Void> handler) {
    int concurrency = getConcurrency(scale().getFactoredPartitionCount());
    log.info("concurrency for messageIntermediateThrowEventConsumer/pod: {}", concurrency);
    return register(
        factory,
        handler::handle,
        MessageEventRequest.class,
        SERVER_INTERMEDIATE_MESSAGE_THROW_EVENT_TOPIC,
        concurrency);
  }

  /**
   * Creates the consumer for signal intermediate throw events.
   *
   * @param factory the Kafka listener container factory
   * @param handler the signal event handler
   * @return the configured consumer factory
   */
  @Bean
  public DynamicKafkaConsumerFactory<SignalEventRequest> signalIntermediateThrowEventConsumer(
      @Qualifier(ORCHEST_ENGINE_CONSUMER_FACTORY_BEAN_NAME)
          ConcurrentKafkaListenerContainerFactory<String, String> factory,
      EventHandler<SignalEventRequest, Void> handler) {
    int concurrency = getConcurrency(scale().getFactoredPartitionCount());
    log.info("concurrency for signalIntermediateThrowEventConsumer/pod: {}", concurrency);
    return register(
        factory,
        handler::handle,
        SignalEventRequest.class,
        SERVER_INTERMEDIATE_SIGNAL_THROW_EVENT_TOPIC,
        concurrency);
  }

  /**
   * Creates the consumer for pending task registration events.
   *
   * @param factory the Kafka listener container factory
   * @param handler the pending task event handler
   * @return the configured consumer factory
   */
  @Bean
  public DynamicKafkaConsumerFactory<PendingTaskRequest> pendingTaskRegisterEventConsumer(
      @Qualifier(ORCHEST_ENGINE_CONSUMER_FACTORY_BEAN_NAME)
          ConcurrentKafkaListenerContainerFactory<String, String> factory,
      EventHandler<PendingTaskRequest, Void> handler) {
    int concurrency = getConcurrency(scale().getFactoredPartitionCount());
    log.info("concurrency for pendingTaskRegisterEventConsumer/pod: {}", concurrency);
    return register(
        factory,
        handler::handle,
        PendingTaskRequest.class,
        SERVER_PENDING_TASK_EVENT_TOPIC,
        concurrency);
  }

  /**
   * Creates the consumer for dynamic process invocation events.
   *
   * @param factory the Kafka listener container factory
   * @param handler the dynamic process invocation event handler
   * @return the configured consumer factory
   */
  @Bean
  public DynamicKafkaConsumerFactory<DynamicProcessInvocationRequest>
      dynamicProcessInstanceEventConsumer(
          @Qualifier(ORCHEST_ENGINE_CONSUMER_FACTORY_BEAN_NAME)
              ConcurrentKafkaListenerContainerFactory<String, String> factory,
          EventHandler<DynamicProcessInvocationRequest, Void> handler) {
    int concurrency = getConcurrency(scale().getFactoredPartitionCount());
    log.info("concurrency for dynamicProcessInstanceEventConsumer/pod: {}", concurrency);
    return register(
        factory,
        handler::handle,
        DynamicProcessInvocationRequest.class,
        DYNAMIC_PROCESS_INVOCATION_EVENT_TOPIC,
        concurrency);
  }

  /**
   * Creates the worker event consumer loader that manages per-process dynamic consumers.
   *
   * @param orchestEngineKafkaListenerContainerFactory the listener container factory
   * @param kafkaAdmin the Kafka admin client
   * @param processDefinitionRepository the process definition repository
   * @param workerEventRequestEventHandler the worker event handler
   * @return the configured WorkerEventConsumerLoader
   */
  @Bean
  public WorkerEventConsumerLoader workerEventConsumerLoader(
      @Qualifier(ORCHEST_ENGINE_CONSUMER_FACTORY_BEAN_NAME)
          ConcurrentKafkaListenerContainerFactory<String, String>
              orchestEngineKafkaListenerContainerFactory,
      @Qualifier(ORCHEST_ENGINE_KAFKA_ADMIN_BEAN_NAME) KafkaAdmin kafkaAdmin,
      ProcessDefinitionRepository processDefinitionRepository,
      EventHandler<WorkerEventRequest, Void> workerEventRequestEventHandler) {
    List<WorkerConsumerConfig> serverWorkerEventConsumerConfigs =
        processDefinitionRepository.findDistinctProcessIds().stream()
            .map(
                processDefinition -> {
                  String serverWorkerEventTopic =
                      getServerWorkerEventTopic(processDefinition.getDefinitionId());
                  return WorkerConsumerConfig.builder()
                      .id(serverWorkerEventTopic)
                      .topic(serverWorkerEventTopic)
                      .groupId(KafkaUtils.getGroupIdWithEnvSuffix(serverWorkerEventTopic))
                      .build();
                })
            .toList();
    Consumer<WorkerEventRequest> consumer = workerEventRequestEventHandler::handle;
    WorkerEventConsumerLoader workerEventConsumerLoader =
        new WorkerEventConsumerLoader(
            orchestEngineKafkaListenerContainerFactory,
            encryptionClient,
            serverWorkerEventConsumerConfigs,
            consumer,
            engineProperties.getEngineScale(),
            kafkaAdmin);
    workerEventConsumerLoader.ensureAllTopicsExist(serverWorkerEventConsumerConfigs);
    return workerEventConsumerLoader;
  }

  /**
   * V1 consumer for client sending event to same Worker Topic And will be replaced by
   * WorkerEventConsumerLoader in future eventually
   */
  @Bean
  public DynamicKafkaConsumerFactory<WorkerEventRequest> workerEventConsumer(
      @Qualifier(ORCHEST_ENGINE_CONSUMER_FACTORY_BEAN_NAME)
          ConcurrentKafkaListenerContainerFactory<String, String> factory,
      EventHandler<WorkerEventRequest, Void> handler) {
    int concurrency = getConcurrency(scale().getPartitionCount());
    log.info("concurrency for workerEventConsumer/pod: {}", concurrency);
    return register(
        factory, handler::handle, WorkerEventRequest.class, SERVER_WORKER_EVENT_TOPIC, concurrency);
  }

  /**
   * Creates the consumer for retry process instance events.
   *
   * @param factory the Kafka listener container factory
   * @param handler the retry process event handler
   * @return the configured consumer factory
   */
  @Bean
  public DynamicKafkaConsumerFactory<RetryProcessEvent> retryProcessInstanceEventConsumer(
      @Qualifier(ORCHEST_ENGINE_CONSUMER_FACTORY_BEAN_NAME)
          ConcurrentKafkaListenerContainerFactory<String, String> factory,
      EventHandler<RetryProcessEvent, Void> handler) {
    int concurrency = getConcurrency(scale().getDefaultPartitionCount());
    log.info("concurrency for retryProcessInstanceEventConsumer/pod: {}", concurrency);
    return register(
        factory,
        handler::handle,
        RetryProcessEvent.class,
        SERVER_RETRY_PROCESS_EVENT_TOPIC,
        concurrency);
  }

  /**
   * Creates the consumer for worker registry events.
   *
   * @param factory the Kafka listener container factory
   * @param handler the worker registry event handler
   * @return the configured consumer factory
   */
  @Bean
  public DynamicKafkaConsumerFactory<WorkerRegistryRequest> workerRegistryEventConsumer(
      @Qualifier(ORCHEST_ENGINE_CONSUMER_FACTORY_BEAN_NAME)
          ConcurrentKafkaListenerContainerFactory<String, String> factory,
      EventHandler<WorkerRegistryRequest, Void> handler) {
    int concurrency = scale().getDefaultPartitionCount();
    log.info("concurrency for workerRegistryEventConsumer/pod: {}", concurrency);
    return register(
        factory,
        handler::handle,
        WorkerRegistryRequest.class,
        CLIENT_WORKER_REGISTER_EVENT_TOPIC,
        concurrency);
  }

  private <T> DynamicKafkaConsumerFactory<T> register(
      ConcurrentKafkaListenerContainerFactory<String, String> factory,
      Consumer<T> handler,
      Class<T> type,
      String topic,
      int concurrency) {
    DynamicKafkaConsumerFactory<T> consumer =
        new DynamicKafkaConsumerFactory<>(factory, handler, encryptionClient, type);
    consumer.registerWorkerConsumer(
        getTopicWithEnvSuffix(topic), getGroupIdWithEnvSuffix(topic), concurrency);
    return consumer;
  }

  private EngineScale scale() {
    return engineProperties.getEngineScale();
  }

  private int getConcurrency(int partitionCount) {
    return EngineScale.getConcurrency(partitionCount, engineProperties.getBrokerCount());
  }
}
