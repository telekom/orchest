package io.telekom.orchest.client.listener;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import io.telekom.orchest.adapter.kafka.client.KafkaEncryptionClient;
import io.telekom.orchest.client.processor.OrchestJobWorkerAnnotationProcessor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.listener.ConcurrentMessageListenerContainer;

/** Tests for {@link DynamicKafkaConsumer} lifecycle, registration, and de-duplication behavior. */
@ExtendWith(MockitoExtension.class)
class DynamicKafkaConsumerTest {

  @Mock
  private ConcurrentKafkaListenerContainerFactory<String, String> kafkaListenerContainerFactory;

  @Mock private OrchestJobWorkerAnnotationProcessor annotationProcessor;

  @Mock private KafkaEncryptionClient kafkaEncryptionClient;

  @Mock private ConcurrentMessageListenerContainer<String, String> mockContainer;

  private DynamicKafkaConsumer dynamicKafkaConsumer;

  @BeforeEach
  void setUp() {
    dynamicKafkaConsumer =
        new DynamicKafkaConsumer(
            kafkaListenerContainerFactory, annotationProcessor, kafkaEncryptionClient);
  }

  // ============================================================
  // registerWorkerConsumer creates and starts container
  // ============================================================

  @Test
  @DisplayName("registerWorkerConsumer creates container, sets concurrency, and starts it")
  void registerWorkerConsumer_createsAndStartsContainer() {
    when(kafkaListenerContainerFactory.createContainer(
            "ORCHEST_CLIENT_WORKER_EVENT_TOPIC_MY_PROCESS"))
        .thenReturn(mockContainer);

    dynamicKafkaConsumer.registerWorkerConsumer("ORCHEST_CLIENT_WORKER_EVENT_TOPIC_MY_PROCESS", 3);

    verify(kafkaListenerContainerFactory)
        .createContainer("ORCHEST_CLIENT_WORKER_EVENT_TOPIC_MY_PROCESS");
    verify(mockContainer).setConcurrency(3);
    verify(mockContainer).start();
  }

  // ============================================================
  // registerWorkerConsumer same topic twice - only one container
  // ============================================================

  @Test
  @DisplayName("registerWorkerConsumer same topic twice creates only one container")
  void registerWorkerConsumer_sameTopicTwice_onlyOneContainer() {
    when(kafkaListenerContainerFactory.createContainer(
            "ORCHEST_CLIENT_WORKER_EVENT_TOPIC_MY_PROCESS"))
        .thenReturn(mockContainer);

    dynamicKafkaConsumer.registerWorkerConsumer("ORCHEST_CLIENT_WORKER_EVENT_TOPIC_MY_PROCESS", 3);
    dynamicKafkaConsumer.registerWorkerConsumer("ORCHEST_CLIENT_WORKER_EVENT_TOPIC_MY_PROCESS", 5);

    // Factory should only be called once because computeIfAbsent prevents duplicates
    verify(kafkaListenerContainerFactory, times(1))
        .createContainer("ORCHEST_CLIENT_WORKER_EVENT_TOPIC_MY_PROCESS");
    verify(mockContainer, times(1)).start();
  }

  // ============================================================
  // stop shuts down all containers
  // ============================================================

  @SuppressWarnings("unchecked")
  @Test
  @DisplayName("stop shuts down all registered containers and clears map")
  void stop_shutsDownAllContainers() {
    ConcurrentMessageListenerContainer<String, String> container1 =
        mock(ConcurrentMessageListenerContainer.class);
    ConcurrentMessageListenerContainer<String, String> container2 =
        mock(ConcurrentMessageListenerContainer.class);

    when(kafkaListenerContainerFactory.createContainer("topic-A")).thenReturn(container1);
    when(kafkaListenerContainerFactory.createContainer("topic-B")).thenReturn(container2);

    dynamicKafkaConsumer.registerWorkerConsumer("topic-A", 1);
    dynamicKafkaConsumer.registerWorkerConsumer("topic-B", 2);

    dynamicKafkaConsumer.start(); // set running = true
    assertTrue(dynamicKafkaConsumer.isRunning());

    dynamicKafkaConsumer.stop();

    verify(container1).stop();
    verify(container2).stop();
    assertFalse(dynamicKafkaConsumer.isRunning());
  }

  // ============================================================
  // stopConsumer removes specific container
  // ============================================================

  @Test
  @DisplayName("stopConsumer stops and removes a specific container")
  void stopConsumer_stopsSpecificContainer() {
    when(kafkaListenerContainerFactory.createContainer("topic-X")).thenReturn(mockContainer);
    dynamicKafkaConsumer.registerWorkerConsumer("topic-X", 2);

    dynamicKafkaConsumer.stopConsumer("topic-X");

    verify(mockContainer).stop();
  }

  @Test
  @DisplayName("stopConsumer with non-existent topic does not throw")
  void stopConsumer_nonExistentTopic_doesNotThrow() {
    assertDoesNotThrow(() -> dynamicKafkaConsumer.stopConsumer("non-existent-topic"));
  }

  // ============================================================
  // Lifecycle methods
  // ============================================================

  @Test
  @DisplayName("start sets running to true")
  void start_setsRunningTrue() {
    assertFalse(dynamicKafkaConsumer.isRunning());

    dynamicKafkaConsumer.start();

    assertTrue(dynamicKafkaConsumer.isRunning());
  }

  @Test
  @DisplayName("stop sets running to false")
  void stop_setsRunningFalse() {
    dynamicKafkaConsumer.start();
    assertTrue(dynamicKafkaConsumer.isRunning());

    dynamicKafkaConsumer.stop();

    assertFalse(dynamicKafkaConsumer.isRunning());
  }

  @Test
  @DisplayName("getPhase returns Integer.MAX_VALUE")
  void getPhase_returnsMaxValue() {
    assertEquals(Integer.MAX_VALUE, dynamicKafkaConsumer.getPhase());
  }
}
