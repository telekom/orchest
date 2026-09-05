package de.telekom.solutions.kmsclient;

import com.amazonaws.encryptionsdk.AwsCrypto;
import com.amazonaws.encryptionsdk.CryptoMaterialsManager;
import de.telekom.solutions.kmsclient.api.CipherType;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.bouncycastle.util.encoders.Base64;

@Slf4j
@Getter
@RequiredArgsConstructor
public class CryptoService {
  private final AwsCrypto awsCrypto;
  private final CryptoMaterialsManager cryptoMaterialsManager;
  private final boolean enabled;

  private static final Map<String, String> ENCRYPTION_CONTEXT;

  static {
    Map<String, String> context = new HashMap<>();
    context.put("consumer", "deutschland");
    ENCRYPTION_CONTEXT = Collections.unmodifiableMap(context);
  }

  public String cipher(CipherType type, String data) {
    if (!enabled) return data;
    String cipher;
    long start = System.currentTimeMillis();
    if (type.equals(CipherType.ENCRYPT)) cipher = encrypt(data);
    else cipher = decrypt(data);
    log.debug("cipher {} took {} ms", type, System.currentTimeMillis() - start);
    return cipher;
  }

  public String encrypt(String plainText) {
    byte[] plainTextBytes = plainText.getBytes(StandardCharsets.UTF_8);
    byte[] encryptedByteArray =
        awsCrypto
            .encryptData(cryptoMaterialsManager, plainTextBytes, ENCRYPTION_CONTEXT)
            .getResult();
    return Base64.toBase64String(encryptedByteArray);
  }

  public String decrypt(String cipherText) {
    byte[] cipherTextBytes = Base64.decode(cipherText);
    byte[] decryptedByteArray =
        awsCrypto.decryptData(cryptoMaterialsManager, cipherTextBytes).getResult();
    return new String(decryptedByteArray, StandardCharsets.UTF_8);
  }
}
