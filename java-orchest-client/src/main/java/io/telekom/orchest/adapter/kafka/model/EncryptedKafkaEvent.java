package io.telekom.orchest.adapter.kafka.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Wrapper for an encrypted Kafka event payload, carrying the ciphertext as a single string field.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class EncryptedKafkaEvent {
  private String event;
}
