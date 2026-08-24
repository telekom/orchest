package io.telekom.orchest.adapter.kafka.model;

import static org.junit.jupiter.api.Assertions.*;

import java.util.Map;
import org.apache.kafka.common.header.Headers;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/** Tests TypedKafkaEvent factory methods, key extraction, header generation, and null handling. */
class TypedKafkaEventTest {

  // ============================================================
  // Factory method: of(topic, payload)
  // ============================================================

  @Test
  @DisplayName(
      "of(topic, payload) creates event with topic and payload, default UUID key, empty headers")
  void of_topicAndPayload_createsEventWithDefaults() {
    TypedKafkaEvent<String> event = TypedKafkaEvent.of("my-topic", "hello");

    assertEquals("my-topic", event.getTopicName());
    assertEquals("hello", event.getValue());
    // Key should be a UUID (from KafkaEvent default)
    assertNotNull(event.getKey());
    assertFalse(event.getKey().isEmpty());
    // Headers should be empty map
    assertTrue(event.getHeaders().isEmpty());
  }

  // ============================================================
  // Factory method: of(topic, payload, keyExtractor)
  // ============================================================

  @Test
  @DisplayName("of(topic, payload, keyExtractor) uses key extractor for key")
  void of_topicPayloadKeyExtractor_usesKeyExtractor() {
    TypedKafkaEvent<String> event = TypedKafkaEvent.of("topic-a", "data", s -> "custom-key");

    assertEquals("topic-a", event.getTopicName());
    assertEquals("data", event.getValue());
    assertEquals("custom-key", event.getKey());
    assertTrue(event.getHeaders().isEmpty());
  }

  @Test
  @DisplayName("of(topic, payload, keyExtractor) falls back to UUID when extractor returns null")
  void of_keyExtractorReturnsNull_fallsBackToUUID() {
    TypedKafkaEvent<String> event = TypedKafkaEvent.of("topic-b", "data", s -> null);

    // Should fall back to KafkaEvent.super.getKey() which is a UUID
    assertNotNull(event.getKey());
    // UUID format check
    assertEquals(36, event.getKey().length());
  }

  // ============================================================
  // Factory method: of(topic, payload, keyExtractor, headerExtractor)
  // ============================================================

  @Test
  @DisplayName("of(topic, payload, keyExtractor, headerExtractor) generates custom headers")
  void of_allParams_generatesHeaders() {
    TypedKafkaEvent<String> event =
        TypedKafkaEvent.of(
            "topic-c",
            "payload",
            s -> "key-123",
            s -> Map.of("header1", "value1", "header2", "value2"));

    assertEquals("topic-c", event.getTopicName());
    assertEquals("payload", event.getValue());
    assertEquals("key-123", event.getKey());

    Map<String, String> headers = event.getHeaders();
    assertEquals(2, headers.size());
    assertEquals("value1", headers.get("header1"));
    assertEquals("value2", headers.get("header2"));
  }

  // ============================================================
  // getKey with null payload
  // ============================================================

  @Test
  @DisplayName("getKey with null payload falls back to default UUID key")
  void getKey_nullPayload_fallsBackToDefaultKey() {
    TypedKafkaEvent<String> event = TypedKafkaEvent.of("topic-d", null, s -> "should-not-run");

    // payload is null, so keyExtractor should not be invoked; falls back to UUID
    assertNotNull(event.getKey());
  }

  // ============================================================
  // getHeaders with null payload
  // ============================================================

  @Test
  @DisplayName("getHeaders with null payload returns empty map")
  void getHeaders_nullPayload_returnsEmptyMap() {
    TypedKafkaEvent<String> event =
        TypedKafkaEvent.of("topic-e", null, s -> "k", s -> Map.of("h", "v"));

    assertTrue(event.getHeaders().isEmpty());
  }

  // ============================================================
  // getHeaders with null headerExtractor
  // ============================================================

  @Test
  @DisplayName("getHeaders with null headerExtractor returns empty map")
  void getHeaders_nullHeaderExtractor_returnsEmptyMap() {
    TypedKafkaEvent<String> event = TypedKafkaEvent.of("topic-f", "payload", s -> "k");

    assertTrue(event.getHeaders().isEmpty());
  }

  // ============================================================
  // getRecordHeaders converts to Kafka Headers
  // ============================================================

  @Test
  @DisplayName("getRecordHeaders converts string headers to Kafka record headers")
  void getRecordHeaders_convertsToKafkaHeaders() {
    TypedKafkaEvent<String> event =
        TypedKafkaEvent.of("topic-g", "data", s -> "key", s -> Map.of("processId", "pd-123"));

    Headers recordHeaders = event.getRecordHeaders();

    assertNotNull(recordHeaders);
    assertNotNull(recordHeaders.lastHeader("processId"));
    assertEquals("pd-123", new String(recordHeaders.lastHeader("processId").value()));
  }

  @Test
  @DisplayName("getRecordHeaders with empty headers returns empty RecordHeaders")
  void getRecordHeaders_emptyHeaders_returnsEmptyRecordHeaders() {
    TypedKafkaEvent<String> event = TypedKafkaEvent.of("topic-h", "data");

    Headers recordHeaders = event.getRecordHeaders();

    assertNotNull(recordHeaders);
    assertFalse(recordHeaders.iterator().hasNext());
  }
}
