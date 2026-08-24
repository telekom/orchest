package io.telekom.orchest.adapter.kafka.client;

import io.telekom.orchest.adapter.kafka.model.EncryptedKafkaEvent;
import io.telekom.orchest.adapter.kafka.model.KafkaEvent;
import io.telekom.orchest.api.core.utils.JsonMapper;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.clients.producer.RecordMetadata;
import org.apache.kafka.common.header.Header;
import org.apache.kafka.common.header.Headers;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;

/** Low-level Kafka producer client that sends plain and encrypted messages to topics. */
@Slf4j
@RequiredArgsConstructor
public class KafkaClient {

  private final KafkaTemplate<String, String> kafkaTemplate;
  private final IEncryptionClient IEncryptionClient;

  /**
   * Sends an unencrypted message to the topic specified in the event.
   *
   * @param kafkaEvent the event containing topic, key, payload, and headers
   */
  public void sendMessage(KafkaEvent<?> kafkaEvent) {
    try {
      final ProducerRecord<String, String> record =
          new ProducerRecord<>(
              kafkaEvent.getTopicName(),
              null,
              kafkaEvent.getKey(),
              JsonMapper.writeToJson(kafkaEvent.getValue()),
              kafkaEvent.getRecordHeaders());
      kafkaTemplate
          .send(record)
          .whenComplete(
              (stringKafkaBeanSendResult, error) -> {
                if (error != null) {
                  onKafkaProducingFailure(kafkaEvent.getTopicName(), error, record);
                } else {
                  onKafkaProducingSuccess(stringKafkaBeanSendResult, record);
                }
              });
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  /**
   * Encrypts the event payload and sends it to the topic with an "encrypted" header.
   *
   * @param kafkaEvent the event containing topic, key, payload, and headers
   */
  public void sendEncryptedMessage(KafkaEvent<?> kafkaEvent) {
    try {
      String encrypt = IEncryptionClient.encrypt(JsonMapper.writeToJson(kafkaEvent.getValue()));
      EncryptedKafkaEvent encryptedKafkaEvent = new EncryptedKafkaEvent(encrypt);
      Headers headers =
          kafkaEvent
              .getRecordHeaders()
              .add(
                  "encrypted",
                  String.valueOf(IEncryptionClient.isEncryptionEnabled())
                      .getBytes(StandardCharsets.UTF_8));
      final ProducerRecord<String, String> record =
          new ProducerRecord<>(
              kafkaEvent.getTopicName(),
              null,
              kafkaEvent.getKey(),
              JsonMapper.writeToJson(encryptedKafkaEvent),
              headers);
      kafkaTemplate
          .send(record)
          .whenComplete(
              (stringKafkaBeanSendResult, error) -> {
                if (error != null) {
                  onKafkaProducingFailure(kafkaEvent.getTopicName(), error, record);
                } else {
                  onKafkaProducingSuccess(stringKafkaBeanSendResult, record);
                }
              });
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  private void onKafkaProducingSuccess(
      SendResult<String, String> stringKafkaBeanSendResult,
      ProducerRecord<String, String> producerRecord) {
    RecordMetadata recordMetadata = stringKafkaBeanSendResult.getRecordMetadata();
    log.debug(
        "Sent Kafka message successfully to topic={} partition={} with offset={} eventType={} key={} eventId={}",
        recordMetadata.topic(),
        recordMetadata.partition(),
        recordMetadata.offset(),
        convertSingleByteHeaderToString(producerRecord, "eventType"),
        producerRecord.key(),
        convertSingleByteHeaderToString(producerRecord, "eventId"));
  }

  private void onKafkaProducingFailure(
      String outboundTopic, Throwable ex, ProducerRecord<String, String> producerRecord) {
    log.error(
        "Unable to send Kafka message to topic={} key={} headers={} message={}",
        outboundTopic,
        producerRecord.key(),
        convertByteHeadersToString(producerRecord.headers()),
        producerRecord.value(),
        ex);
    throw new RuntimeException(ex);
  }

  /**
   * Reads a single header value from a producer record as a UTF-8 string.
   *
   * @param producerRecord the record to read from
   * @param key the header key
   * @return the header value as string, or null if not present
   */
  public String convertSingleByteHeaderToString(
      ProducerRecord<String, String> producerRecord, String key) {
    if (producerRecord.headers().lastHeader(key) != null) {
      return new String(producerRecord.headers().lastHeader(key).value(), StandardCharsets.UTF_8);
    } else {
      return null;
    }
  }

  /**
   * Converts all headers to a list of "key=value" strings for logging.
   *
   * @param headers the Kafka headers to convert
   * @return list of header key-value pairs as strings
   */
  public List<String> convertByteHeadersToString(Headers headers) {
    List<String> stringHeaders = new ArrayList<>();
    for (Header header : headers) {
      String h = header.key() + "=" + new String(header.value(), StandardCharsets.UTF_8);
      stringHeaders.add(h);
    }
    return stringHeaders;
  }
}
