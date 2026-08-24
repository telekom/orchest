package io.telekom.orchest.kafka;

import io.telekom.orchest.adapter.kafka.model.EncryptedKafkaEvent;
import io.telekom.orchest.adapter.kafka.model.KafkaEvent;
import io.telekom.orchest.api.core.adapters.cipher.CipherType;
import io.telekom.orchest.api.core.utils.JsonMapper;
import io.telekom.orchest.kafka.kms.IEncryptionClient;
import java.nio.charset.StandardCharsets;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.header.Headers;

/**
 * Kafka messaging client that sends events as producer records, with optional encryption via KMS.
 */
@Slf4j
@RequiredArgsConstructor
public class KafkaClient {

  private final KafkaProducerManager kafkaProducerManager;
  private final IEncryptionClient encryptionClient;

  /**
   * Sends a Kafka event as a plaintext producer record.
   *
   * @param kafkaEvent the event to send
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
      kafkaProducerManager.sendSync(record);

    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  /**
   * Encrypts the event payload and sends it as a Kafka producer record with an encryption header.
   *
   * @param kafkaEvent the event to encrypt and send
   */
  public void sendEncryptedMessage(KafkaEvent<?> kafkaEvent) {
    try {
      String encrypt =
          encryptionClient.cipher(
              CipherType.ENCRYPT, JsonMapper.writeToJson(kafkaEvent.getValue()));
      EncryptedKafkaEvent encryptedKafkaEvent = new EncryptedKafkaEvent(encrypt);
      Headers headers =
          kafkaEvent
              .getRecordHeaders()
              .add(
                  "encrypted",
                  String.valueOf(encryptionClient.isEncryptionEnabled())
                      .getBytes(StandardCharsets.UTF_8));
      final ProducerRecord<String, String> record =
          new ProducerRecord<>(
              kafkaEvent.getTopicName(),
              null,
              kafkaEvent.getKey(),
              JsonMapper.writeToJson(encryptedKafkaEvent),
              headers);
      kafkaProducerManager.sendSync(record);
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }
}
