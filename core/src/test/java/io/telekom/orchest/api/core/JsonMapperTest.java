package io.telekom.orchest.api.core;

import static org.junit.jupiter.api.Assertions.*;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import io.telekom.orchest.api.core.model.bpmn.NodeType;
import io.telekom.orchest.api.core.model.bpmn.node.BaseNode;
import io.telekom.orchest.api.core.model.bpmn.node.ServiceTaskNode;
import io.telekom.orchest.api.core.model.bpmn.node.TaskNode;
import io.telekom.orchest.api.core.utils.JsonMapper;
import java.math.BigDecimal;
import java.time.Duration;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

/**
 * Unit tests for {@link JsonMapper} serialization, deserialization, and polymorphic type handling.
 */
class JsonMapperTest {

  private static final String TASK_NODE_CLASS =
      "io.telekom.orchest.api.core.model.bpmn.node.TaskNode";

  @Nested
  class ReadFromJsonWithClass {

    @Test
    void shouldDeserializeValidJson() {
      String json = "{\"orderId\":\"123\",\"amount\":99}";

      Map result = JsonMapper.readFromJson(json, Map.class);

      assertEquals("123", result.get("orderId"));
      assertEquals(99, result.get("amount"));
    }

    @Test
    void shouldIgnoreUnknownProperties() {
      String json =
          "{\"@class\":\""
              + TASK_NODE_CLASS
              + "\","
              + "\"id\":\"task-1\",\"name\":\"My Task\",\"unknownField\":\"ignored\"}";

      TaskNode node = JsonMapper.readFromJson(json, TaskNode.class);

      assertEquals("task-1", node.getId());
      assertEquals("My Task", node.getName());
    }

    @Test
    void shouldThrowRuntimeExceptionForInvalidJson() {
      String invalidJson = "not-json{{{";

      RuntimeException ex =
          assertThrows(
              RuntimeException.class, () -> JsonMapper.readFromJson(invalidJson, Map.class));

      assertEquals("failed to map", ex.getMessage());
      assertNotNull(ex.getCause());
    }

    @Test
    void shouldReturnEmptyMapForEmptyJsonObject() {
      String json = "{}";

      Map result = JsonMapper.readFromJson(json, Map.class);

      assertNotNull(result);
      assertTrue(result.isEmpty());
    }

    @Test
    void shouldDeserializeJsonArray() {
      String json = "[1,2,3]";

      List result = JsonMapper.readFromJson(json, List.class);

      assertEquals(3, result.size());
      assertEquals(1, result.get(0));
    }

    @Test
    void shouldDeserializeBooleanValues() {
      String json = "{\"active\":true,\"deleted\":false}";

      Map result = JsonMapper.readFromJson(json, Map.class);

      assertEquals(true, result.get("active"));
      assertEquals(false, result.get("deleted"));
    }

    @Test
    void shouldDeserializeNestedObjects() {
      String json = "{\"outer\":{\"inner\":\"value\"}}";

      Map result = JsonMapper.readFromJson(json, Map.class);

      assertNotNull(result.get("outer"));
      assertInstanceOf(Map.class, result.get("outer"));
    }

    @Test
    void shouldThrowForNullJson() {
      assertThrows(RuntimeException.class, () -> JsonMapper.readFromJson(null, Map.class));
    }

    @Test
    void shouldThrowForEmptyString() {
      assertThrows(RuntimeException.class, () -> JsonMapper.readFromJson("", Map.class));
    }
  }

  @Nested
  class ReadFromJsonWithTypeReference {

    @Test
    void shouldDeserializeListOfStrings() {
      String json = "[\"alpha\",\"beta\",\"gamma\"]";

      List<String> result = JsonMapper.readFromJson(json, new TypeReference<List<String>>() {});

      assertEquals(3, result.size());
      assertEquals("alpha", result.get(0));
      assertEquals("beta", result.get(1));
      assertEquals("gamma", result.get(2));
    }

    @Test
    void shouldDeserializeMapTypeReference() {
      String json = "{\"key\":\"value\"}";

      Map<String, String> result =
          JsonMapper.readFromJson(json, new TypeReference<Map<String, String>>() {});

      assertEquals("value", result.get("key"));
    }

    @Test
    void shouldThrowRuntimeExceptionForInvalidJson() {
      RuntimeException ex =
          assertThrows(
              RuntimeException.class,
              () -> JsonMapper.readFromJson("{{bad}}", new TypeReference<List<String>>() {}));

      assertEquals("failed to map", ex.getMessage());
    }

    @Test
    void shouldDeserializeEmptyList() {
      String json = "[]";

      List<String> result = JsonMapper.readFromJson(json, new TypeReference<List<String>>() {});

      assertNotNull(result);
      assertTrue(result.isEmpty());
    }

    @Test
    void shouldDeserializeListOfMaps() {
      String json = "[{\"id\":1},{\"id\":2}]";

      List<Map<String, Integer>> result =
          JsonMapper.readFromJson(json, new TypeReference<List<Map<String, Integer>>>() {});

      assertEquals(2, result.size());
      assertEquals(1, result.get(0).get("id"));
    }
  }

  @Nested
  class WriteToJson {

    @Test
    void shouldSerializeSimpleObject() {
      Map<String, Object> obj = Map.of("name", "test", "value", 42);

      String json = JsonMapper.writeToJson(obj);

      assertNotNull(json);
      assertTrue(json.contains("\"name\""));
      assertTrue(json.contains("\"test\""));
      assertTrue(json.contains("42"));
    }

    @Test
    void shouldExcludeNullFields() {
      TaskNode node = new TaskNode();
      node.setId("task-1");

      String json = JsonMapper.writeToJson(node);

      assertTrue(json.contains("task-1"));
      assertFalse(json.contains("\"name\""));
    }

    @Test
    void shouldSerializeBigDecimalAsString() {
      Map<String, BigDecimal> obj = Map.of("price", new BigDecimal("19.99"));

      String json = JsonMapper.writeToJson(obj);

      assertTrue(json.contains("\"19.99\""));
    }

    @Test
    void shouldNotFailOnEmptyBean() {
      Object emptyBean = new Object() {};

      assertDoesNotThrow(() -> JsonMapper.writeToJson(emptyBean));
    }

    @Test
    void shouldSerializeNullToNullString() {
      String json = JsonMapper.writeToJson(null);

      assertEquals("null", json);
    }

    @Test
    void shouldSerializeDateTimesAsIsoStrings() {
      Map<String, OffsetDateTime> obj =
          Map.of("date", OffsetDateTime.of(2025, 6, 15, 10, 30, 0, 0, ZoneOffset.UTC));

      String json = JsonMapper.writeToJson(obj);

      assertTrue(json.contains("2025-06-15"));
      // Ensure it is NOT an epoch timestamp (long number)
      assertFalse(json.matches(".*\"date\"\\s*:\\s*\\d{10,}.*"));
    }

    @Test
    void shouldSerializeDurationAsIsoString() {
      Map<String, Duration> obj = Map.of("timeout", Duration.ofMinutes(5));

      String json = JsonMapper.writeToJson(obj);

      assertTrue(json.contains("PT5M"));
    }

    @Test
    void shouldSerializeBigDecimalWithHighPrecision() {
      Map<String, BigDecimal> obj = Map.of("precise", new BigDecimal("123456789.123456789"));

      String json = JsonMapper.writeToJson(obj);

      assertTrue(json.contains("123456789.123456789"));
    }

    @Test
    void shouldSerializeEmptyMap() {
      String json = JsonMapper.writeToJson(Map.of());

      assertEquals("{}", json);
    }

    @Test
    void shouldSerializeEmptyList() {
      String json = JsonMapper.writeToJson(List.of());

      assertEquals("[]", json);
    }
  }

  @Nested
  class PolymorphicDeserialization {

    @Test
    void shouldPreserveTaskNodeTypeOnRoundTrip() {
      TaskNode original = new TaskNode("task-1", "My Task");

      String json = JsonMapper.writeToJson(original);
      BaseNode deserialized = JsonMapper.readFromJson(json, BaseNode.class);

      assertInstanceOf(TaskNode.class, deserialized);
      assertEquals("task-1", deserialized.getId());
      assertEquals("My Task", deserialized.getName());
      assertEquals(NodeType.TASK, deserialized.getType());
    }

    @Test
    void shouldPreserveServiceTaskNodeTypeOnRoundTrip() {
      ServiceTaskNode original = new ServiceTaskNode("st-1", "Service Task");

      String json = JsonMapper.writeToJson(original);
      BaseNode deserialized = JsonMapper.readFromJson(json, BaseNode.class);

      assertInstanceOf(ServiceTaskNode.class, deserialized);
      assertEquals("st-1", deserialized.getId());
    }

    @Test
    void shouldIncludeClassTypePropertyInJson() {
      TaskNode node = new TaskNode("task-1", "My Task");

      String json = JsonMapper.writeToJson(node);

      assertTrue(json.contains("@class"));
      assertTrue(json.contains("TaskNode"));
    }
  }

  @Nested
  class ReadTree {

    @Test
    void shouldReturnJsonNodeForValidJson() {
      String json = "{\"key\":\"value\",\"nested\":{\"a\":1}}";

      JsonNode tree = JsonMapper.readTree(json);

      assertEquals("value", tree.get("key").asText());
      assertEquals(1, tree.get("nested").get("a").asInt());
    }

    @Test
    void shouldThrowRuntimeExceptionForInvalidJson() {
      RuntimeException ex =
          assertThrows(RuntimeException.class, () -> JsonMapper.readTree("not valid json"));

      assertEquals("failed to map", ex.getMessage());
    }

    @Test
    void shouldParseJsonArray() {
      String json = "[1,2,3]";

      JsonNode tree = JsonMapper.readTree(json);

      assertTrue(tree.isArray());
      assertEquals(3, tree.size());
    }

    @Test
    void shouldParseNullJsonValue() {
      String json = "{\"key\":null}";

      JsonNode tree = JsonMapper.readTree(json);

      assertTrue(tree.get("key").isNull());
    }

    @Test
    void shouldThrowForNullInput() {
      assertThrows(RuntimeException.class, () -> JsonMapper.readTree(null));
    }
  }

  @Nested
  class ConvertValue {

    @Test
    void shouldConvertMapToObjectWithClass() {
      Map<String, Object> map =
          Map.of(
              "@class", TASK_NODE_CLASS,
              "id", "task-1",
              "name", "Task");

      TaskNode node = JsonMapper.convertValue(map, TaskNode.class);

      assertEquals("task-1", node.getId());
      assertEquals("Task", node.getName());
    }

    @Test
    void shouldConvertWithTypeReference() {
      List<String> original = List.of("a", "b", "c");

      List<String> result = JsonMapper.convertValue(original, new TypeReference<List<String>>() {});

      assertEquals(3, result.size());
      assertEquals("a", result.get(0));
    }

    @Test
    void shouldConvertNullToNull() {
      TaskNode result = JsonMapper.convertValue(null, TaskNode.class);

      assertNull(result);
    }
  }

  @Nested
  class RoundTripTests {

    @Test
    void shouldRoundTripSimpleMap() {
      Map<String, Object> original = Map.of("key", "value", "num", 42);

      String json = JsonMapper.writeToJson(original);
      Map result = JsonMapper.readFromJson(json, Map.class);

      assertEquals("value", result.get("key"));
      assertEquals(42, result.get("num"));
    }

    @Test
    void shouldRoundTripListOfStrings() {
      List<String> original = List.of("a", "b", "c");

      String json = JsonMapper.writeToJson(original);
      List<String> result = JsonMapper.readFromJson(json, new TypeReference<List<String>>() {});

      assertEquals(original, result);
    }

    @Test
    void shouldRoundTripBigDecimalPreservingPrecision() {
      Map<String, BigDecimal> original = Map.of("amount", new BigDecimal("99.95"));

      String json = JsonMapper.writeToJson(original);

      // BigDecimal serialized as string "99.95", so on readback it is a string
      assertTrue(json.contains("\"99.95\""));
    }
  }
}
