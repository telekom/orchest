package io.telekom.orchest.adapter.kafka.model;

import java.util.Map;
import java.util.UUID;
import org.apache.kafka.common.header.Headers;
import org.apache.kafka.common.header.internals.RecordHeaders;

/**
 * Extended event record interface that provides default key generation and Kafka header conversion.
 *
 * @param <T> the event payload type
 */
public interface KafkaEvent<T> extends IEventRecordDTO<T> {

  /**
   * Returns a record key for Kafka partitioning. Defaults to a random UUID.
   *
   * @return the partition key
   */
  default String getKey() {
    return UUID.randomUUID().toString();
  }

  /**
   * Converts the string header map into Kafka {@link Headers} for the producer record.
   *
   * @return populated Kafka headers
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
