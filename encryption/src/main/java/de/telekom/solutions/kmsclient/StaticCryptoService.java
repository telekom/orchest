package de.telekom.solutions.kmsclient;

import com.amazonaws.encryptionsdk.AwsCrypto;
import com.amazonaws.encryptionsdk.CryptoMaterialsManager;
import de.telekom.solutions.kmsclient.api.CipherType;
import jakarta.annotation.PostConstruct;
import java.nio.charset.StandardCharsets;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.bouncycastle.util.encoders.Base64;

@Slf4j
@RequiredArgsConstructor
public class StaticCryptoService {
  private final CryptoService cryptoService;

  private static AwsCrypto staticAWSCrypto;
  private static CryptoMaterialsManager staticCachingCryptoMaterialsManager;
  private static boolean enabled;

  public static String cipher(CipherType type, String data) {
    if (!enabled) return data;
    String cipher;
    long start = System.currentTimeMillis();
    if (type.equals(CipherType.ENCRYPT)) cipher = encrypt(data);
    else cipher = decrypt(data);
    log.debug("cipher {} took {} ms", type, System.currentTimeMillis() - start);
    return cipher;
  }

  public static String encrypt(String plainText) {
    byte[] plainTextBytes = plainText.getBytes(StandardCharsets.UTF_8);
    byte[] encryptedByteArray =
        staticAWSCrypto
            .encryptData(staticCachingCryptoMaterialsManager, plainTextBytes)
            .getResult();
    return Base64.toBase64String(encryptedByteArray);
  }

  public static String decrypt(String cipherText) {
    byte[] cipherTextBytes = Base64.decode(cipherText);
    byte[] decryptedByteArray =
        staticAWSCrypto
            .decryptData(staticCachingCryptoMaterialsManager, cipherTextBytes)
            .getResult();
    return new String(decryptedByteArray, StandardCharsets.UTF_8);
  }

  @PostConstruct
  void setClient() {
    staticAWSCrypto = cryptoService.getAwsCrypto();
    staticCachingCryptoMaterialsManager = cryptoService.getCryptoMaterialsManager();
    enabled = cryptoService.isEnabled();
  }
}
