package io.telekom.orchest.adapter.kafka.model;

import java.util.Map;
import java.util.function.Function;

/**
 * Generic Kafka event implementation that replaces per-type boilerplate event classes. Supports
 * custom key extraction and header generation via functional interfaces.
 *
 * @param <T> The type of the event payload.
 */
public class TypedKafkaEvent<T> implements KafkaEvent<T> {

  private final String topic;
  private final T payload;
  private final Function<T, String> keyExtractor;
  private final Function<T, Map<String, String>> headerExtractor;

  private TypedKafkaEvent(
      String topic,
      T payload,
      Function<T, String> keyExtractor,
      Function<T, Map<String, String>> headerExtractor) {
    this.topic = topic;
    this.payload = payload;
    this.keyExtractor = keyExtractor;
    this.headerExtractor = headerExtractor;
  }

  /**
   * Creates a typed event with a random key and no custom headers.
   *
   * @param topic the target Kafka topic
   * @param payload the event payload
   * @param <T> the payload type
   * @return a new TypedKafkaEvent instance
   */
  public static <T> TypedKafkaEvent<T> of(String topic, T payload) {
    return new TypedKafkaEvent<>(topic, payload, null, null);
  }

  /**
   * Creates a typed event with a custom key extractor and no custom headers.
   *
   * @param topic the target Kafka topic
   * @param payload the event payload
   * @param keyExtractor function to derive the message key from the payload
   * @param <T> the payload type
   * @return a new TypedKafkaEvent instance
   */
  public static <T> TypedKafkaEvent<T> of(
      String topic, T payload, Function<T, String> keyExtractor) {
    return new TypedKafkaEvent<>(topic, payload, keyExtractor, null);
  }

  /**
   * Creates a typed event with custom key and header extractors.
   *
   * @param topic the target Kafka topic
   * @param payload the event payload
   * @param keyExtractor function to derive the message key from the payload
   * @param headerExtractor function to derive headers from the payload
   * @param <T> the payload type
   * @return a new TypedKafkaEvent instance
   */
  public static <T> TypedKafkaEvent<T> of(
      String topic,
      T payload,
      Function<T, String> keyExtractor,
      Function<T, Map<String, String>> headerExtractor) {
    return new TypedKafkaEvent<>(topic, payload, keyExtractor, headerExtractor);
  }

  /** {@inheritDoc} */
  @Override
  public String getTopicName() {
    return topic;
  }

  /** {@inheritDoc} */
  @Override
  public T getValue() {
    return payload;
  }

  /** {@inheritDoc} */
  @Override
  public String getKey() {
    if (keyExtractor != null && payload != null) {
      String key = keyExtractor.apply(payload);
      if (key != null) return key;
    }
    return KafkaEvent.super.getKey();
  }

  /** {@inheritDoc} */
  @Override
  public Map<String, String> getHeaders() {
    if (headerExtractor != null && payload != null) {
      return headerExtractor.apply(payload);
    }
    return Map.of();
  }
}
