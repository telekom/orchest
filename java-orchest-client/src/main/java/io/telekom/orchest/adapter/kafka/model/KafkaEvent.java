package io.telekom.orchest.adapter.kafka.model;

import java.util.Map;
import java.util.UUID;
import org.apache.kafka.common.header.Headers;
import org.apache.kafka.common.header.internals.RecordHeaders;

/**
 * Extended event record contract that provides Kafka-native header conversion and a default random
 * key.
 *
 * @param <T> the type of the event payload
 */
public interface KafkaEvent<T> extends IEventRecordDTO<T> {

  /**
   * Returns a random UUID as the default message key.
   *
   * @return a random UUID string
   */
  default String getKey() {
    return UUID.randomUUID().toString();
  }

  /**
   * Converts the header map into Kafka-native {@link Headers}.
   *
   * @return the Kafka record headers
   */
  default Headers getRecordHeaders() {
    Headers recordHeaders = new RecordHeaders();
    Map<String, String> headers = getHeaders();
    if (headers != null) {
      for (Map.Entry<String, String> entry : headers.entrySet()) {
        recordHeaders.add(entry.getKey(), entry.getValue().getBytes());
      }
    }
    return recordHeaders;
  }
}
