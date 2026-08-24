package io.telekom.orchest.api.core.utils;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fasterxml.jackson.module.scala.DefaultScalaModule;
import java.math.BigDecimal;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;

/**
 * Utility class for JSON serialization and deserialization using Jackson. Provides configured
 * ObjectMapper instances and helper methods for common operations.
 */
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public class JsonMapper {

  private static final String MAPPING_EXCEPTION_ERROR_MESSAGE = "failed to map";
  private static final ObjectMapper objectMapper = new ObjectMapper();
  private static final ObjectMapper objectMapperWithScalaModule = new ObjectMapper();

  static {
    addCommonConfigs(objectMapper);
    addCommonConfigs(objectMapperWithScalaModule);
    objectMapperWithScalaModule.registerModule(new DefaultScalaModule());
  }

  private static void addCommonConfigs(ObjectMapper objectMapper) {
    objectMapper.registerModule(new JavaTimeModule());
    objectMapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
    objectMapper.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);
    objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    objectMapper.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);
    objectMapper.configure(SerializationFeature.WRITE_DURATIONS_AS_TIMESTAMPS, false);

    SimpleModule bigDecimalToString = new SimpleModule();
    bigDecimalToString.addSerializer(BigDecimal.class, new ToStringSerializer());
    objectMapper.registerModule(bigDecimalToString);
  }

  /**
   * Deserializes a JSON string into an object of the specified class.
   *
   * @param json The JSON string to parse.
   * @param clazz The target class.
   * @param <T> The type of the object.
   * @return The deserialized object.
   * @throws RuntimeException if mapping fails.
   */
  public static <T> T readFromJson(String json, Class<T> clazz) {
    try {
      return objectMapper.readValue(json, clazz);
    } catch (Exception e) {
      throw new RuntimeException(MAPPING_EXCEPTION_ERROR_MESSAGE, e);
    }
  }

  /**
   * Deserializes a JSON string using a TypeReference.
   *
   * @param json The JSON string to parse.
   * @param typeReference The TypeReference describing the target type.
   * @param <T> The type of the object.
   * @return The deserialized object.
   * @throws RuntimeException if mapping fails.
   */
  public static <T> T readFromJson(String json, TypeReference<T> typeReference) {
    try {
      return objectMapper.readValue(json, typeReference);
    } catch (Exception e) {
      throw new RuntimeException(MAPPING_EXCEPTION_ERROR_MESSAGE, e);
    }
  }

  /**
   * Parses a JSON string into a JsonNode tree.
   *
   * @param json The JSON string to parse.
   * @return The root JsonNode.
   * @throws RuntimeException if mapping fails.
   */
  public static JsonNode readTree(String json) {
    try {
      return objectMapper.readTree(json);
    } catch (Exception e) {
      throw new RuntimeException(MAPPING_EXCEPTION_ERROR_MESSAGE, e);
    }
  }

  /**
   * Converts a value to a given type using a TypeReference.
   *
   * @param input The input object to convert.
   * @param typeReference The target type reference.
   * @param <T> The target type.
   * @return The converted object.
   * @throws RuntimeException if conversion fails.
   */
  public static <T> T convertValue(Object input, TypeReference<T> typeReference) {
    try {
      return objectMapper.convertValue(input, typeReference);
    } catch (Exception e) {
      throw new RuntimeException(MAPPING_EXCEPTION_ERROR_MESSAGE, e);
    }
  }

  /**
   * Converts a value to a given class type.
   *
   * @param input The input object to convert.
   * @param clazz The target class.
   * @param <T> The target type.
   * @return The converted object.
   * @throws RuntimeException if conversion fails.
   */
  public static <T> T convertValue(Object input, Class<T> clazz) {
    try {
      return objectMapper.convertValue(input, clazz);
    } catch (Exception e) {
      throw new RuntimeException(MAPPING_EXCEPTION_ERROR_MESSAGE, e);
    }
  }

  /**
   * Converts a value to a given class type using the Scala-enabled ObjectMapper.
   *
   * @param input The input object to convert.
   * @param clazz The target class.
   * @param <T> The target type.
   * @return The converted object.
   * @throws RuntimeException if conversion fails.
   */
  public static <T> T convertValueWithScalaSupport(Object input, Class<T> clazz) {
    try {
      return objectMapperWithScalaModule.convertValue(input, clazz);
    } catch (Exception e) {
      throw new RuntimeException(MAPPING_EXCEPTION_ERROR_MESSAGE, e);
    }
  }

  /**
   * Serializes an object to a JSON string.
   *
   * @param obj The object to serialize.
   * @return The JSON string representation.
   * @throws RuntimeException if serialization fails.
   */
  public static String writeToJson(Object obj) {
    try {
      return objectMapper.writeValueAsString(obj);
    } catch (Exception e) {
      throw new RuntimeException(MAPPING_EXCEPTION_ERROR_MESSAGE, e);
    }
  }

  /**
   * Serializes an object to a JSON string using the Scala-enabled ObjectMapper.
   *
   * @param obj The object to serialize.
   * @return The JSON string representation.
   * @throws RuntimeException if serialization fails.
   */
  public static String writeToJsonWithScalaSupport(Object obj) {
    try {
      return objectMapperWithScalaModule.writeValueAsString(obj);
    } catch (Exception e) {
      throw new RuntimeException(MAPPING_EXCEPTION_ERROR_MESSAGE, e);
    }
  }
}
