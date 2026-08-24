package io.telekom.orchest.connectorservice.handler;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import io.telekom.orchest.api.core.adapters.data.model.ProcessInstance;
import io.telekom.orchest.api.core.model.bpmn.ConnectorException;
import io.telekom.orchest.api.core.model.bpmn.DataMapping;
import io.telekom.orchest.api.core.model.bpmn.node.BaseNode;
import io.telekom.orchest.api.core.model.bpmn.node.TaskNode;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.RecordMetadata;
import org.apache.kafka.common.TopicPartition;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

/** Tests Kafka connector record production, input mapping resolution, and validation. */
@ExtendWith(MockitoExtension.class)
class KafkaConnectorHandlerTest {

  @Mock private Producer<String, String> producer;

  private KafkaConnectorHandler handler;
  private ProcessInstance instance;
  private BaseNode node;

  @BeforeEach
  void setUp() {
    handler =
        new KafkaConnectorHandler() {
          @Override
          protected Producer<String, String> createProducer(
              String bootstrapServers, List<DataMapping> inputMappings, ProcessInstance instance) {
            return producer;
          }
        };
    instance = new ProcessInstance("pi-1", "pd-1", 1);
    instance.setVariables(new HashMap<>());
    node = new TaskNode("node-1", "Kafka Task");
  }

  @Test
  void metadata() {
    assertEquals("io.orchest.connector-kafka:1", new KafkaConnectorHandler().connectorType());
    assertEquals("KAFKA_CONNECTOR_ERROR", new KafkaConnectorHandler().errorCode());
  }

  @Test
  void produce_usingJsonBindingNames() throws Exception {
    node.setInputMappings(
        List.of(
            new DataMapping("topic.bootstrapServers", "localhost:9092"),
            new DataMapping("topic.topicName", "test-topic"),
            new DataMapping("message.key", "my-key"),
            new DataMapping("message.value", "{\"data\":1}")));
    node.setOutputMappings(List.of(new DataMapping("resultVariable", "kafkaResponse")));

    RecordMetadata metadata =
        new RecordMetadata(new TopicPartition("test-topic", 0), 42, 0, 123456789L, 3, 10);
    when(producer.send(any())).thenReturn(CompletableFuture.completedFuture(metadata));

    Map<String, Object> output = handler.execute(instance, node);

    verify(producer).send(any());
    @SuppressWarnings("unchecked")
    Map<String, Object> responseData = (Map<String, Object>) output.get("kafkaResponse");
    assertEquals("test-topic", responseData.get("topic"));
    assertEquals(0, responseData.get("partition"));
    assertEquals(42L, responseData.get("offset"));
  }

  @Test
  void produce_fallsBackToLegacyKeyValue() throws Exception {
    node.setInputMappings(
        List.of(
            new DataMapping("topic.bootstrapServers", "localhost:9092"),
            new DataMapping("topic.topicName", "test-topic"),
            new DataMapping("key", "legacy-key"),
            new DataMapping("value", "{\"legacy\":true}")));
    node.setOutputMappings(List.of(new DataMapping("resultVariable", "kafkaResponse")));

    RecordMetadata metadata =
        new RecordMetadata(new TopicPartition("test-topic", 0), 10, 0, 123L, 3, 10);
    when(producer.send(any())).thenReturn(CompletableFuture.completedFuture(metadata));

    Map<String, Object> output = handler.execute(instance, node);

    verify(producer).send(any());
    assertNotNull(output.get("kafkaResponse"));
  }

  @Test
  void missingBootstrapServers_throws() {
    node.setInputMappings(
        List.of(
            new DataMapping("topic.topicName", "test-topic"),
            new DataMapping("message.value", "data")));
    node.setOutputMappings(new ArrayList<>());
    ConnectorException ex =
        assertThrows(ConnectorException.class, () -> handler.execute(instance, node));
    assertTrue(ex.getMessage().contains("bootstrapServers is required"));
  }

  @Test
  void missingTopic_throws() {
    node.setInputMappings(
        List.of(
            new DataMapping("topic.bootstrapServers", "localhost:9092"),
            new DataMapping("message.value", "data")));
    node.setOutputMappings(new ArrayList<>());
    ConnectorException ex =
        assertThrows(ConnectorException.class, () -> handler.execute(instance, node));
    assertTrue(ex.getMessage().contains("topic is required"));
  }
}
