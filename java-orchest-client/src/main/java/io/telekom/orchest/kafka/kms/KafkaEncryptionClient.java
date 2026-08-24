package io.telekom.orchest.kafka.kms;

import com.amazonaws.encryptionsdk.AwsCrypto;
import com.amazonaws.encryptionsdk.CryptoMaterialsManager;
import com.amazonaws.encryptionsdk.caching.CachingCryptoMaterialsManager;
import com.amazonaws.encryptionsdk.caching.LocalCryptoMaterialsCache;
import io.telekom.orchest.api.core.adapters.cipher.CipherType;
import io.telekom.orchest.config.OrchestClientProperties;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.TimeUnit;
import lombok.extern.slf4j.Slf4j;
import org.bouncycastle.util.encoders.Base64;

/**
 * AWS KMS-based encryption client for Kafka message payloads. Uses the AWS Encryption SDK with
 * caching crypto materials for performance.
 */
@Slf4j
public class KafkaEncryptionClient implements IEncryptionClient {

  private AwsCrypto awsCrypto;
  private CryptoMaterialsManager cachingCryptoMaterialsManager;
  private final boolean enabled;

  /**
   * Creates the encryption client, initializing AWS crypto if encryption is enabled.
   *
   * @param orchestClientProperties configuration properties containing KMS settings
   */
  public KafkaEncryptionClient(OrchestClientProperties orchestClientProperties) {
    OrchestClientProperties.Encryption encryption = orchestClientProperties.getEncryption();
    this.enabled = encryption.isEnableEncryption();
    if (this.enabled) {
      this.awsCrypto = AwsCrypto.standard();
      this.cachingCryptoMaterialsManager =
          CachingCryptoMaterialsManager.newBuilder()
              .withMasterKeyProvider(AWSBuilderUtils.buildKeyProvider(orchestClientProperties))
              .withCache(new LocalCryptoMaterialsCache(1000))
              .withMaxAge(60000, TimeUnit.SECONDS)
              .withMessageUseLimit(1000)
              .build();
    }
  }

  @Override
  public String cipher(CipherType type, String data) {
    if (!enabled) return data;
    String cipher;
    long start = System.currentTimeMillis();
    if (type.equals(CipherType.ENCRYPT)) cipher = encrypt(data);
    else cipher = decrypt(data);
    log.debug("cipher {} took {} ms", type, System.currentTimeMillis() - start);
    return cipher;
  }

  @Override
  public String encrypt(String plainText) {
    byte[] plainTextBytes = plainText.getBytes(StandardCharsets.UTF_8);
    byte[] encryptedByteArray =
        awsCrypto.encryptData(cachingCryptoMaterialsManager, plainTextBytes).getResult();
    return Base64.toBase64String(encryptedByteArray);
  }

  @Override
  public String decrypt(String cipherText) {
    byte[] cipherTextBytes = Base64.decode(cipherText);
    byte[] decryptedByteArray =
        awsCrypto.decryptData(cachingCryptoMaterialsManager, cipherTextBytes).getResult();
    return new String(decryptedByteArray, StandardCharsets.UTF_8);
  }

  @Override
  public boolean isEncryptionEnabled() {
    return enabled;
  }
}
