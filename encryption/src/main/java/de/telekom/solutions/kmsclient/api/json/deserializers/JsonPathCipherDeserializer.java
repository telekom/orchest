package de.telekom.solutions.kmsclient.api.json.deserializers;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.databind.deser.ContextualDeserializer;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import de.telekom.solutions.kmsclient.StaticCryptoService;
import de.telekom.solutions.kmsclient.api.CipherType;
import de.telekom.solutions.kmsclient.api.utils.SerDerUtils;
import java.io.IOException;
import java.util.Map;

public class JsonPathCipherDeserializer extends StdDeserializer<String>
    implements ContextualDeserializer {

  private final Map<String, Map<String, String>> encryptionPathMap;

  private boolean decrypt;

  public JsonPathCipherDeserializer(Map<String, Map<String, String>> encryptionPathMap) {
    super(String.class);
    this.encryptionPathMap = encryptionPathMap;
  }

  @Override
  public String deserialize(JsonParser jsonParser, DeserializationContext deserializationContext)
      throws IOException {
    if (decrypt) return StaticCryptoService.cipher(CipherType.DECRYPT, jsonParser.getText());
    return jsonParser.getText();
  }

  @Override
  public JsonDeserializer<?> createContextual(
      DeserializationContext deserializationContext, BeanProperty beanProperty) {
    String className = beanProperty.getMember().getDeclaringClass().getName();
    String propertyName = beanProperty.getName();
    if (SerDerUtils.pathMatched(className, propertyName, encryptionPathMap)) decrypt = true;
    return new JsonPathCipherDeserializer(encryptionPathMap);
  }
}
