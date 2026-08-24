package io.telekom.orchest.connectorservice.handler;

import static io.telekom.orchest.enginecore.bpmn.utils.IODataMappingsUtils.getDataMappingValue;

import io.telekom.orchest.api.core.adapters.data.model.ProcessInstance;
import io.telekom.orchest.api.core.model.bpmn.ConnectorException;
import io.telekom.orchest.api.core.model.bpmn.DataMapping;
import io.telekom.orchest.api.core.model.bpmn.node.BaseNode;
import io.telekom.orchest.api.core.utils.JsonMapper;
import io.telekom.orchest.enginecore.bpmn.utils.VariablesUtils;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.TimeUnit;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.clients.producer.RecordMetadata;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.stereotype.Component;

/** Kafka connector handler. Produces a record to an external Kafka topic. */
@Slf4j
@Component
public class KafkaConnectorHandler implements ConnectorHandler {

  private static final long SEND_TIMEOUT_SECONDS = 30;

  @Override
  public String connectorType() {
    return "io.orchest.connector-kafka:1";
  }

  @Override
  public String errorCode() {
    return "KAFKA_CONNECTOR_ERROR";
  }

  @Override
  public Map<String, Object> execute(ProcessInstance instance, BaseNode node) {
    List<DataMapping> inputMappings = node.getInputMappings();
    List<DataMapping> outputMappings = node.getOutputMappings();

    String bootstrapServers = evaluate("topic.bootstrapServers", inputMappings, instance);
    String topic = evaluate("topic.topicName", inputMappings, instance);
    if (topic == null) {
      topic = evaluate("topic", inputMappings, instance);
    }
    String key = evaluate("message.key", inputMappings, instance);
    if (key == null) {
      key = evaluate("key", inputMappings, instance);
    }
    String value =
        getDataMappingValue("message.value", inputMappings)
            .or(() -> getDataMappingValue("value", inputMappings))
            .map(v -> VariablesUtils.getEvaluatedVariable(v, instance.getVariables()))
            .map(obj -> obj instanceof String string ? string : JsonMapper.writeToJson(obj))
            .orElse(null);
    String resultVariable;
    try {
      resultVariable = getDataMappingValue("resultVariable", outputMappings).get();
    } catch (Exception e) {
      resultVariable = "kafkaConnectorResponse";
    }

    if (bootstrapServers == null || bootstrapServers.isBlank()) {
      throw new ConnectorException("bootstrapServers is required for Kafka connector");
    }
    if (topic == null || topic.isBlank()) {
      throw new ConnectorException("topic is required for Kafka connector");
    }

    try (Producer<String, String> producer =
        createProducer(bootstrapServers, inputMappings, instance)) {
      ProducerRecord<String, String> record = new ProducerRecord<>(topic, key, value);
      RecordMetadata metadata = producer.send(record).get(SEND_TIMEOUT_SECONDS, TimeUnit.SECONDS);

      log.info(
          "Kafka connector produced record for node: {} to topic: {} partition: {} offset: {}",
          node.getName(),
          metadata.topic(),
          metadata.partition(),
          metadata.offset());

      Map<String, Object> responseData = new HashMap<>();
      responseData.put("topic", metadata.topic());
      responseData.put("partition", metadata.partition());
      responseData.put("offset", metadata.offset());
      responseData.put("timestamp", metadata.timestamp());
      return Map.of(resultVariable, responseData);
    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
      throw new ConnectorException("Kafka connector send interrupted: " + e.getMessage(), e);
    } catch (ConnectorException e) {
      throw e;
    } catch (Exception e) {
      throw new ConnectorException("Kafka connector execution failed: " + e.getMessage(), e);
    }
  }

  /** Creates a Kafka producer. Extracted for testability. */
  protected Producer<String, String> createProducer(
      String bootstrapServers, List<DataMapping> inputMappings, ProcessInstance instance) {
    Properties props = new Properties();
    props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
    props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
    props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
    props.put(ProducerConfig.ACKS_CONFIG, "all");

    String saslMechanism = evaluate("authentication.method", inputMappings, instance);
    String username = evaluate("authentication.username", inputMappings, instance);
    String password = evaluate("authentication.password", inputMappings, instance);
    if (saslMechanism != null && username != null && password != null) {
      props.put("security.protocol", "SASL_SSL");
      props.put("sasl.mechanism", saslMechanism.toUpperCase());
      props.put(
          "sasl.jaas.config",
          String.format(
              "org.apache.kafka.common.security.scram.ScramLoginModule required username=\"%s\" password=\"%s\";",
              username, password));
    }
    return new KafkaProducer<>(props);
  }

  private String evaluate(String key, List<DataMapping> inputMappings, ProcessInstance instance) {
    return getDataMappingValue(key, inputMappings)
        .map(value -> VariablesUtils.getEvaluatedVariable(value, instance.getVariables()))
        .map(Object::toString)
        .orElse(null);
  }
}
