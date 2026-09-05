package de.telekom.solutions.kmsclient.api.json.deserializers;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.BeanProperty;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.deser.ContextualDeserializer;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import de.telekom.solutions.kmsclient.StaticCryptoService;
import de.telekom.solutions.kmsclient.api.CipherType;
import de.telekom.solutions.kmsclient.api.annotations.Cipher;
import java.io.IOException;

public class AnnotationCipherDeserializer extends StdDeserializer<String>
    implements ContextualDeserializer {

  private boolean decrypt;

  public AnnotationCipherDeserializer() {
    super(String.class);
  }

  public AnnotationCipherDeserializer(boolean decrypt) {
    super(String.class);
    this.decrypt = decrypt;
  }

  @Override
  public String deserialize(JsonParser jsonParser, DeserializationContext deserializationContext)
      throws IOException {
    JsonNode node = jsonParser.getCodec().readTree(jsonParser);
    if (node.getNodeType().name().equals("STRING") && decrypt)
      return StaticCryptoService.cipher(CipherType.DECRYPT, node.asText());
    else return node.asText();
  }

  @Override
  public JsonDeserializer<?> createContextual(
      DeserializationContext deserializationContext, BeanProperty property) {
    boolean decryptEnabled = false;
    Cipher cipher = null;
    if (property != null) {
      cipher = property.getAnnotation(Cipher.class);
    }
    if (cipher != null) {
      decryptEnabled = true;
    }
    return new AnnotationCipherDeserializer(decryptEnabled);
  }
}
