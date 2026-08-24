package io.telekom.orchest.adapter.kafka;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import io.telekom.orchest.adapter.kafka.client.IEncryptionClient;
import io.telekom.orchest.adapter.kafka.model.EncryptedKafkaEvent;
import io.telekom.orchest.api.core.utils.JsonMapper;
import java.nio.charset.StandardCharsets;
import java.util.Optional;
import java.util.function.Consumer;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.common.header.internals.RecordHeader;
import org.apache.kafka.common.header.internals.RecordHeaders;
import org.apache.kafka.common.record.TimestampType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.listener.ConcurrentMessageListenerContainer;
import org.springframework.kafka.listener.ContainerProperties;
import org.springframework.kafka.listener.MessageListener;

/** Tests dynamic Kafka consumer creation, lifecycle management, and message decryption handling. */
@ExtendWith(MockitoExtension.class)
class DynamicKafkaConsumerFactoryTest {

  @Mock
  private ConcurrentKafkaListenerContainerFactory<String, String> kafkaListenerContainerFactory;

  @Mock private Consumer<String> consumer;

  @Mock private IEncryptionClient encryptionClient;

  @Mock private ConcurrentMessageListenerContainer<String, String> mockContainer;

  private DynamicKafkaConsumerFactory<String> factory;

  @BeforeEach
  void setUp() {
    // Production code sets groupId via getContainerProperties(); Mockito returns null by default.
    // Lenient: many tests never touch mockContainer ( Mockito strict stubbing otherwise fails).
    lenient()
        .when(mockContainer.getContainerProperties())
        .thenReturn(new ContainerProperties("dummy-topic"));
    factory =
        new DynamicKafkaConsumerFactory<>(
            kafkaListenerContainerFactory, consumer, encryptionClient, String.class);
  }

  // ============================================================
  // registerWorkerConsumer - creates and starts container
  // ============================================================

  @Test
  @DisplayName(
      "registerWorkerConsumer creates container with correct topic, concurrency, beanName and starts it")
  void registerWorkerConsumer_createsAndStartsContainer() {
    when(kafkaListenerContainerFactory.createContainer("test-topic")).thenReturn(mockContainer);

    factory.registerWorkerConsumer("test-topic", "test-bean", 5);

    verify(kafkaListenerContainerFactory).createContainer("test-topic");
    verify(mockContainer).setConcurrency(5);
    verify(mockContainer).setBeanName("test-bean");
    verify(mockContainer).setupMessageListener(any(MessageListener.class));
    verify(mockContainer).start();
  }

  // ============================================================
  // registerWorkerConsumer - same topic twice creates only one
  // ============================================================

  @Test
  @DisplayName("registerWorkerConsumer called twice with same topic creates only one container")
  void registerWorkerConsumer_sameTopicTwice_onlyOneContainer() {
    when(kafkaListenerContainerFactory.createContainer("dup-topic")).thenReturn(mockContainer);

    factory.registerWorkerConsumer("dup-topic", "bean-1", 3);
    factory.registerWorkerConsumer("dup-topic", "bean-2", 5);

    verify(kafkaListenerContainerFactory, times(1)).createContainer("dup-topic");
    verify(mockContainer, times(1)).start();
  }

  // ============================================================
  // Message listener - malformed JSON (JsonMapper throws)
  // ============================================================

  @SuppressWarnings("unchecked")
  @Test
  @DisplayName("Message listener propagates RuntimeException when record value is not valid JSON")
  void messageListener_malformedJson_throwsRuntimeException() {
    ArgumentCaptor<MessageListener<String, String>> listenerCaptor =
        ArgumentCaptor.forClass(MessageListener.class);
    when(kafkaListenerContainerFactory.createContainer("poison-topic")).thenReturn(mockContainer);

    factory.registerWorkerConsumer("poison-topic", "poison-bean", 1);

    verify(mockContainer).setupMessageListener(listenerCaptor.capture());
    MessageListener<String, String> listener = listenerCaptor.getValue();

    ConsumerRecord<String, String> badRecord =
        new ConsumerRecord<>("poison-topic", 0, 0L, "key", "not-valid-json{{{");

    RuntimeException thrown =
        assertThrows(RuntimeException.class, () -> listener.onMessage(badRecord));
    assertEquals("failed to map", thrown.getMessage());

    verify(consumer, never()).accept(any());
  }

  // ============================================================
  // Message listener - processes unencrypted message
  // ============================================================

  @SuppressWarnings("unchecked")
  @Test
  @DisplayName("Message listener processes unencrypted message when no 'encrypted' header present")
  void messageListener_unencryptedMessage_processedDirectly() {
    ArgumentCaptor<MessageListener<String, String>> listenerCaptor =
        ArgumentCaptor.forClass(MessageListener.class);
    when(kafkaListenerContainerFactory.createContainer("plain-topic")).thenReturn(mockContainer);

    factory.registerWorkerConsumer("plain-topic", "plain-bean", 1);

    verify(mockContainer).setupMessageListener(listenerCaptor.capture());
    MessageListener<String, String> listener = listenerCaptor.getValue();

    // Create a record with a plain JSON string value (no "encrypted" header)
    ConsumerRecord<String, String> record =
        new ConsumerRecord<>("plain-topic", 0, 0L, "key", "\"hello-world\"");

    listener.onMessage(record);

    verify(consumer).accept("hello-world");
    verify(encryptionClient, never()).decrypt(any());
  }

  // ============================================================
  // Message listener - processes encrypted message
  // ============================================================

  @SuppressWarnings("unchecked")
  @Test
  @DisplayName("Message listener decrypts and processes message when 'encrypted' header is present")
  void messageListener_encryptedMessage_decryptsAndProcesses() {
    ArgumentCaptor<MessageListener<String, String>> listenerCaptor =
        ArgumentCaptor.forClass(MessageListener.class);
    when(kafkaListenerContainerFactory.createContainer("enc-topic")).thenReturn(mockContainer);

    factory.registerWorkerConsumer("enc-topic", "enc-bean", 1);

    verify(mockContainer).setupMessageListener(listenerCaptor.capture());
    MessageListener<String, String> listener = listenerCaptor.getValue();

    // Build an encrypted event payload
    EncryptedKafkaEvent encryptedEvent = new EncryptedKafkaEvent("encrypted-data");
    String jsonPayload = JsonMapper.writeToJson(encryptedEvent);

    RecordHeaders headers = new RecordHeaders();
    headers.add(new RecordHeader("encrypted", "true".getBytes(StandardCharsets.UTF_8)));

    ConsumerRecord<String, String> record =
        new ConsumerRecord<>(
            "enc-topic",
            0,
            0L,
            0L,
            TimestampType.CREATE_TIME,
            -1,
            -1,
            "key",
            jsonPayload,
            headers,
            java.util.Optional.empty());

    when(encryptionClient.decrypt("encrypted-data")).thenReturn("\"decrypted-value\"");

    listener.onMessage(record);

    verify(encryptionClient).decrypt("encrypted-data");
    verify(consumer).accept("decrypted-value");
  }

  // ============================================================
  // stopConsumer - stops and removes container
  // ============================================================

  @Test
  @DisplayName("stopConsumer stops and removes the container for the given topic")
  void stopConsumer_stopsAndRemovesContainer() {
    when(kafkaListenerContainerFactory.createContainer("stop-topic")).thenReturn(mockContainer);
    factory.registerWorkerConsumer("stop-topic", "stop-bean", 1);

    factory.stopConsumer("stop-topic");

    verify(mockContainer).stop();
  }

  @Test
  @DisplayName("stopConsumer with non-existent topic does not throw")
  void stopConsumer_nonExistentTopic_doesNotThrow() {
    assertDoesNotThrow(() -> factory.stopConsumer("non-existent"));
  }

  // ============================================================
  // getHeaderValue
  // ============================================================

  @Test
  @DisplayName("getHeaderValue returns value when header exists")
  void getHeaderValue_headerExists_returnsValue() {
    RecordHeaders headers = new RecordHeaders();
    headers.add(new RecordHeader("encrypted", "true".getBytes(StandardCharsets.UTF_8)));

    ConsumerRecord<String, String> record =
        new ConsumerRecord<>(
            "topic",
            0,
            0L,
            0L,
            TimestampType.CREATE_TIME,
            -1,
            -1,
            "key",
            "value",
            headers,
            java.util.Optional.empty());

    Optional<String> result = factory.getHeaderValue(record, "encrypted");

    assertTrue(result.isPresent());
    assertEquals("true", result.get());
  }

  @Test
  @DisplayName("getHeaderValue returns empty when header does not exist")
  void getHeaderValue_headerMissing_returnsEmpty() {
    ConsumerRecord<String, String> record = new ConsumerRecord<>("topic", 0, 0L, "key", "value");

    Optional<String> result = factory.getHeaderValue(record, "encrypted");

    assertFalse(result.isPresent());
  }

  // ============================================================
  // Lifecycle methods
  // ============================================================

  @Test
  @DisplayName("start sets running to true")
  void start_setsRunningTrue() {
    assertFalse(factory.isRunning());

    factory.start();

    assertTrue(factory.isRunning());
  }

  @Test
  @DisplayName("stop shuts down all containers and sets running to false")
  @SuppressWarnings("unchecked")
  void stop_shutsDownAllContainers() {
    ConcurrentMessageListenerContainer<String, String> container1 =
        mock(ConcurrentMessageListenerContainer.class);
    ConcurrentMessageListenerContainer<String, String> container2 =
        mock(ConcurrentMessageListenerContainer.class);
    when(container1.getContainerProperties()).thenReturn(new ContainerProperties("t1"));
    when(container2.getContainerProperties()).thenReturn(new ContainerProperties("t2"));

    when(kafkaListenerContainerFactory.createContainer("t1")).thenReturn(container1);
    when(kafkaListenerContainerFactory.createContainer("t2")).thenReturn(container2);

    factory.registerWorkerConsumer("t1", "b1", 1);
    factory.registerWorkerConsumer("t2", "b2", 2);
    factory.start();
    assertTrue(factory.isRunning());

    factory.stop();

    verify(container1).stop();
    verify(container2).stop();
    assertFalse(factory.isRunning());
  }

  @Test
  @DisplayName("stop handles exception from one container gracefully")
  @SuppressWarnings("unchecked")
  void stop_containerStopThrows_handledGracefully() {
    ConcurrentMessageListenerContainer<String, String> container1 =
        mock(ConcurrentMessageListenerContainer.class);
    ConcurrentMessageListenerContainer<String, String> container2 =
        mock(ConcurrentMessageListenerContainer.class);
    when(container1.getContainerProperties()).thenReturn(new ContainerProperties("t1"));
    when(container2.getContainerProperties()).thenReturn(new ContainerProperties("t2"));

    when(kafkaListenerContainerFactory.createContainer("t1")).thenReturn(container1);
    when(kafkaListenerContainerFactory.createContainer("t2")).thenReturn(container2);
    doThrow(new RuntimeException("stop failed")).when(container1).stop();

    factory.registerWorkerConsumer("t1", "b1", 1);
    factory.registerWorkerConsumer("t2", "b2", 1);
    factory.start();

    // Should not propagate exception
    assertDoesNotThrow(() -> factory.stop());
    assertFalse(factory.isRunning());
  }

  @Test
  @DisplayName("getPhase returns Integer.MAX_VALUE for late shutdown")
  void getPhase_returnsMaxValue() {
    assertEquals(Integer.MAX_VALUE, factory.getPhase());
  }
}
