package io.telekom.orchest.adapter.kafka.model;

import java.util.Map;

/**
 * Base contract for a Kafka event record containing key, topic, payload, and headers.
 *
 * @param <T> the event payload type
 */
public interface IEventRecordDTO<T> {

  /**
   * Returns the Kafka record key used for partitioning.
   *
   * @return the record key
   */
  String getKey();

  /**
   * Returns the target Kafka topic name.
   *
   * @return the topic name
   */
  String getTopicName();

  /**
   * Returns the event payload.
   *
   * @return the payload value
   */
  T getValue();

  /**
   * Returns additional headers to attach to the Kafka record.
   *
   * @return map of header key-value pairs
   */
  Map<String, String> getHeaders();
}
