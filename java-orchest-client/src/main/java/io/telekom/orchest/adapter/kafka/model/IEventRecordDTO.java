package io.telekom.orchest.adapter.kafka.model;

import java.util.Map;

/**
 * Generic contract for a Kafka event record, providing access to key, topic, payload, and headers.
 *
 * @param <T> the type of the event payload
 */
public interface IEventRecordDTO<T> {

  /**
   * Returns the Kafka message key used for partitioning.
   *
   * @return the message key
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
   * Returns custom headers to attach to the Kafka record.
   *
   * @return map of header name to header value
   */
  Map<String, String> getHeaders();
}
