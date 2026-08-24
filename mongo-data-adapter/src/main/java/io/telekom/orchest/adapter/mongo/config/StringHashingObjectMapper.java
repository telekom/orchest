package io.telekom.orchest.adapter.mongo.config;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import java.io.IOException;
import java.math.BigDecimal;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.apache.commons.codec.digest.DigestUtils;

/**
 * ObjectMapper variant that hashes all String values using SHA-256 during both serialization and
 * deserialization. Used to produce searchable but irreversible representations of variables.
 */
@Getter
public class StringHashingObjectMapper {

  private final ObjectMapper mapper;

  /**
   * Creates the ObjectMapper with SHA-256 hashing serializers/deserializers for all String fields.
   */
  public StringHashingObjectMapper() {
    ObjectMapper stringHashingObjectMapper = new ObjectMapper();
    SimpleModule bigDecimalToString = new SimpleModule();
    bigDecimalToString.addSerializer(BigDecimal.class, new ToStringSerializer());

    stringHashingObjectMapper.registerModule(new JavaTimeModule());

    stringHashingObjectMapper.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);
    stringHashingObjectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    stringHashingObjectMapper.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);
    stringHashingObjectMapper.configure(SerializationFeature.WRITE_DURATIONS_AS_TIMESTAMPS, false);

    stringHashingObjectMapper.registerModule(bigDecimalToString);

    SimpleModule encryptionDeserializer = new SimpleModule();
    encryptionDeserializer.addDeserializer(String.class, new StringValueHashingDeserializer());
    encryptionDeserializer.addSerializer(String.class, new StringValueHashingSerializer());
    stringHashingObjectMapper.registerModules(encryptionDeserializer);
    this.mapper = stringHashingObjectMapper;
  }

  @RequiredArgsConstructor
  public static class StringValueHashingDeserializer extends JsonDeserializer<String> {

    @Override
    public String deserialize(JsonParser jsonParser, DeserializationContext deserializationContext)
        throws IOException {
      return DigestUtils.sha256Hex(jsonParser.getText());
    }
  }

  @RequiredArgsConstructor
  public static class StringValueHashingSerializer extends JsonSerializer<String> {

    @Override
    public void serialize(
        String s, JsonGenerator jsonGenerator, SerializerProvider serializerProvider)
        throws IOException {
      jsonGenerator.writeString(DigestUtils.sha256Hex(s));
    }
  }
}
