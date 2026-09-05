package de.telekom.solutions.kmsclient.api.json.serializers;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.BeanProperty;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.ContextualSerializer;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import de.telekom.solutions.kmsclient.StaticCryptoService;
import de.telekom.solutions.kmsclient.api.CipherType;
import de.telekom.solutions.kmsclient.api.annotations.Cipher;
import java.io.IOException;

public class AnnotationCipherSerializer extends StdSerializer<String>
    implements ContextualSerializer {

  private boolean encrypt;

  public AnnotationCipherSerializer() {
    super(String.class);
  }

  public AnnotationCipherSerializer(boolean encrypt) {
    super(String.class);
    this.encrypt = encrypt;
  }

  @Override
  public void serialize(
      String value, JsonGenerator jsonGenerator, SerializerProvider serializerProvider)
      throws IOException {
    if (encrypt)
      jsonGenerator.writeString(
          String.format(StaticCryptoService.cipher(CipherType.ENCRYPT, value)));
    else jsonGenerator.writeString(value);
  }

  @Override
  public JsonSerializer<?> createContextual(SerializerProvider prov, BeanProperty property) {
    boolean encryptEnabled = false;
    Cipher cipher = null;
    if (property != null) {
      cipher = property.getAnnotation(Cipher.class);
    }
    if (cipher != null) {
      encryptEnabled = true;
    }
    return new AnnotationCipherSerializer(encryptEnabled);
  }
}
