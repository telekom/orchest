package de.telekom.solutions.kmsclient.api.json.serializers;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import de.telekom.solutions.kmsclient.StaticCryptoService;
import de.telekom.solutions.kmsclient.api.CipherType;
import de.telekom.solutions.kmsclient.api.utils.SerDerUtils;
import java.io.IOException;
import java.util.Map;

public class JsonPathCipherSerializer extends StdSerializer<String> {

  private final Map<String, Map<String, String>> encryptionPathMap;

  public JsonPathCipherSerializer(Map<String, Map<String, String>> encryptionPathMap) {
    super(String.class);
    this.encryptionPathMap = encryptionPathMap;
  }

  @Override
  public void serialize(String value, JsonGenerator generator, SerializerProvider provider)
      throws IOException {
    boolean encryptValue = false;
    Object currentClass = provider.getGenerator().getOutputContext().getCurrentValue();
    if (currentClass != null) {
      encryptValue = SerDerUtils.pathMatched(currentClass, provider, encryptionPathMap);
    }
    if (encryptValue) generator.writeString(StaticCryptoService.cipher(CipherType.ENCRYPT, value));
    else generator.writeString(value);
  }
}
