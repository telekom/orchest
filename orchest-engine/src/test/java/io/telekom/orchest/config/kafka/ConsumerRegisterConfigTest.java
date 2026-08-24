package io.telekom.orchest.config.kafka;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

import io.telekom.orchest.adapter.kafka.DynamicKafkaConsumerFactory;
import io.telekom.orchest.adapter.kafka.client.IEncryptionClient;
import io.telekom.orchest.adapter.kafka.config.OrchestKafkaProperties;
import io.telekom.orchest.api.core.adapters.data.dto.WorkerEventRequest;
import io.telekom.orchest.api.core.properties.EngineScale;
import io.telekom.orchest.api.core.request.*;
import io.telekom.orchest.config.engine.EngineProperties;
import io.telekom.orchest.enginecore.bpmn.eventhandler.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.listener.ConcurrentMessageListenerContainer;
import org.springframework.kafka.listener.ContainerProperties;

/**
 * Tests for {@link ConsumerRegisterConfig} verifying that all Kafka consumer beans are created
 * correctly with proper concurrency settings based on engine scale.
 */
@ExtendWith(MockitoExtension.class)
class ConsumerRegisterConfigTest {

  @Mock private IEncryptionClient encryptionClient;

  @Mock private EngineProperties engineProperties;

  @Mock private OrchestKafkaProperties kafkaAdapterProperties;

  @Mock private ConcurrentKafkaListenerContainerFactory<String, String> factory;

  @Mock private ConcurrentMessageListenerContainer<String, String> container;

  @Mock private IDeploymentEventHandler deploymentEventHandler;

  @Mock private IMessageIntermediateThrowEventEventHandler messageThrowHandler;

  @Mock private ISignalIntermediateThrowEventEventHandler signalThrowHandler;

  @Mock private IPendingTaskRegisterEventHandler pendingTaskHandler;

  @Mock private IProcessInvocationEventHandler processInvocationHandler;

  @Mock private IDynamicProcessInvocationEventHandler dynamicProcessHandler;

  @Mock private IWorkerEventEventHandler workerEventHandler;

  @Mock private IRetryProcessInstanceEventHandler retryProcessHandler;

  @Mock private IWorkerRegistryEventHandler workerRegistryHandler;

  @Mock private ApplicationEventPublisher applicationEventPublisher;

  private ConsumerRegisterConfig config;

  @BeforeEach
  void setUp() {
    lenient().when(engineProperties.getEngineScale()).thenReturn(EngineScale.LOCAL);
    // Mockito mocks return 0 for int getters; getConcurrency divides by brokerCount — must be
    // non-zero.
    lenient().when(engineProperties.getBrokerCount()).thenReturn(3);
    lenient().when(kafkaAdapterProperties.getReplicaCount()).thenReturn(3);
    // Factory.createContainer() must return a non-null container mock
    lenient().when(factory.createContainer(anyString())).thenReturn(container);
    lenient().when(container.getContainerProperties()).thenReturn(new ContainerProperties("dummy"));

    config =
        new ConsumerRegisterConfig(encryptionClient, engineProperties, applicationEventPublisher);
  }

  @Test
  @DisplayName("deploymentEventConsumer should create a DynamicKafkaConsumerFactory")
  void deploymentEventConsumer_shouldCreateFactory() {
    DynamicKafkaConsumerFactory<ResourceDeploymentRequest> consumer =
        config.deploymentEventConsumer(factory, deploymentEventHandler);
    assertNotNull(consumer);
  }

  @Test
  @DisplayName("messageIntermediateThrowEventConsumer should create a DynamicKafkaConsumerFactory")
  void messageIntermediateThrowEventConsumer_shouldCreateFactory() {
    DynamicKafkaConsumerFactory<MessageEventRequest> consumer =
        config.messageIntermediateThrowEventConsumer(factory, messageThrowHandler);
    assertNotNull(consumer);
  }

  @Test
  @DisplayName("signalIntermediateThrowEventConsumer should create a DynamicKafkaConsumerFactory")
  void signalIntermediateThrowEventConsumer_shouldCreateFactory() {
    DynamicKafkaConsumerFactory<SignalEventRequest> consumer =
        config.signalIntermediateThrowEventConsumer(factory, signalThrowHandler);
    assertNotNull(consumer);
  }

  @Test
  @DisplayName("pendingTaskRegisterEventConsumer should create a DynamicKafkaConsumerFactory")
  void pendingTaskRegisterEventConsumer_shouldCreateFactory() {
    DynamicKafkaConsumerFactory<PendingTaskRequest> consumer =
        config.pendingTaskRegisterEventConsumer(factory, pendingTaskHandler);
    assertNotNull(consumer);
  }

  @Test
  @DisplayName("processInstanceEventConsumer should create a DynamicKafkaConsumerFactory")
  void processInstanceEventConsumer_shouldCreateFactory() {
    DynamicKafkaConsumerFactory<ProcessInvocationRequest> consumer =
        config.processInstanceEventConsumer(factory, processInvocationHandler);
    assertNotNull(consumer);
  }

  @Test
  @DisplayName("dynamicProcessInstanceEventConsumer should create a DynamicKafkaConsumerFactory")
  void dynamicProcessInstanceEventConsumer_shouldCreateFactory() {
    DynamicKafkaConsumerFactory<DynamicProcessInvocationRequest> consumer =
        config.dynamicProcessInstanceEventConsumer(factory, dynamicProcessHandler);
    assertNotNull(consumer);
  }

  @Test
  @DisplayName("workerEventConsumer should create a DynamicKafkaConsumerFactory")
  void workerEventConsumer_shouldCreateFactory() {
    DynamicKafkaConsumerFactory<WorkerEventRequest> consumer =
        config.workerEventConsumer(factory, workerEventHandler);
    assertNotNull(consumer);
  }

  @Test
  @DisplayName("retryProcessInstanceEventConsumer should create a DynamicKafkaConsumerFactory")
  void retryProcessInstanceEventConsumer_shouldCreateFactory() {
    DynamicKafkaConsumerFactory<RetryProcessEvent> consumer =
        config.retryProcessInstanceEventConsumer(factory, retryProcessHandler);
    assertNotNull(consumer);
  }

  @Test
  @DisplayName("workerRegistryEventConsumer should create a DynamicKafkaConsumerFactory")
  void workerRegistryEventConsumer_shouldCreateFactory() {
    DynamicKafkaConsumerFactory<WorkerRegistryRequest> consumer =
        config.workerRegistryEventConsumer(factory, workerRegistryHandler);
    assertNotNull(consumer);
  }

  @Test
  @DisplayName("All consumers should use engine scale from EngineProperties")
  void allConsumers_shouldUseEngineScaleFromProperties() {
    config.deploymentEventConsumer(factory, deploymentEventHandler);
    config.messageIntermediateThrowEventConsumer(factory, messageThrowHandler);
    config.signalIntermediateThrowEventConsumer(factory, signalThrowHandler);
    config.pendingTaskRegisterEventConsumer(factory, pendingTaskHandler);
    config.processInstanceEventConsumer(factory, processInvocationHandler);
    config.dynamicProcessInstanceEventConsumer(factory, dynamicProcessHandler);
    config.workerEventConsumer(factory, workerEventHandler);
    config.retryProcessInstanceEventConsumer(factory, retryProcessHandler);
    config.workerRegistryEventConsumer(factory, workerRegistryHandler);

    // scale() is called for each consumer bean
    verify(engineProperties, times(9)).getEngineScale();
  }

  @Test
  @DisplayName(
      "All consumers should use brokerCount from EngineProperties for concurrency calculation")
  void allConsumers_shouldUseBrokerCount() {
    config.deploymentEventConsumer(factory, deploymentEventHandler);
    config.processInstanceEventConsumer(factory, processInvocationHandler);
    config.workerEventConsumer(factory, workerEventHandler);

    verify(engineProperties, atLeast(3)).getBrokerCount();
  }

  @Test
  @DisplayName("Should use SMALL scale concurrency when configured")
  void shouldUseSmallScaleConcurrency() {
    when(engineProperties.getEngineScale()).thenReturn(EngineScale.SMALL);

    DynamicKafkaConsumerFactory<WorkerEventRequest> consumer =
        config.workerEventConsumer(factory, workerEventHandler);

    assertNotNull(consumer);
    verify(engineProperties, atLeastOnce()).getEngineScale();
  }

  @Test
  @DisplayName("Should use LARGE scale concurrency when configured")
  void shouldUseLargeScaleConcurrency() {
    when(engineProperties.getEngineScale()).thenReturn(EngineScale.LARGE);

    DynamicKafkaConsumerFactory<ProcessInvocationRequest> consumer =
        config.processInstanceEventConsumer(factory, processInvocationHandler);

    assertNotNull(consumer);
    verify(engineProperties, atLeastOnce()).getEngineScale();
  }

  @Test
  @DisplayName("Factory createContainer should be called once per unique topic")
  void factoryCreateContainer_shouldBeCalledPerTopic() {
    config.deploymentEventConsumer(factory, deploymentEventHandler);
    config.processInstanceEventConsumer(factory, processInvocationHandler);

    // Two different topics -> two createContainer calls
    verify(factory, times(2)).createContainer(anyString());
  }

  @Test
  @DisplayName("Container should have concurrency and bean name set")
  void container_shouldHaveConcurrencyAndBeanNameSet() {
    config.deploymentEventConsumer(factory, deploymentEventHandler);

    // deploymentEventConsumer uses scale().getDefaultPartitionCount() / brokerCount = 3 / 3 = 1
    // (LOCAL)
    verify(container).setConcurrency(1);
    verify(container).setBeanName(anyString());
    verify(container).start();
  }
}
