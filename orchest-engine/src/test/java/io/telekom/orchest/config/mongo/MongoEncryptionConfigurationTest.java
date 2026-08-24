package io.telekom.orchest.config.mongo;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import de.telekom.solutions.kmsclient.CryptoService;
import de.telekom.solutions.kmsclient.KMSClientBuilderService;
import io.telekom.orchest.adapter.mongo.config.StringHashingObjectMapper;
import io.telekom.orchest.adapter.mongo.listners.DecisionInstanceCryptListener;
import io.telekom.orchest.adapter.mongo.listners.ProcessInstanceCryptListener;
import io.telekom.orchest.api.core.adapters.cipher.CipherType;
import io.telekom.orchest.api.core.adapters.cipher.ICipherService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

/**
 * Tests for {@link MongoEncryptionConfiguration} verifying cipher service delegation and listener
 * bean creation.
 */
@ExtendWith(MockitoExtension.class)
class MongoEncryptionConfigurationTest {

  @Mock private KMSClientBuilderService kmsClientBuilderService;

  @Mock private CryptoService mongoRead;

  @Mock private CryptoService mongoWrite;

  private final MongoEncryptionConfiguration configuration = new MongoEncryptionConfiguration();

  @Test
  @DisplayName(
      "cipherService creates bridge delegating encrypt to mongo-write and decrypt to mongo-read")
  void cipherService_delegatesToKmsCryptoServices() {
    when(kmsClientBuilderService.build("mongo-read")).thenReturn(mongoRead);
    when(kmsClientBuilderService.build("mongo-write")).thenReturn(mongoWrite);
    when(mongoWrite.encrypt("plain")).thenReturn("enc");
    when(mongoRead.decrypt("enc")).thenReturn("plain");

    ICipherService cipherService = configuration.cipherService(kmsClientBuilderService);

    assertEquals("enc", cipherService.encrypt("plain"));
    assertEquals("plain", cipherService.decrypt("enc"));
    verify(mongoWrite).encrypt("plain");
    verify(mongoRead).decrypt("enc");
  }

  @Test
  @DisplayName("cipherService cipher(ENCRYPT) delegates to mongo-write")
  void cipherService_cipherEncrypt() {
    when(kmsClientBuilderService.build("mongo-read")).thenReturn(mongoRead);
    when(kmsClientBuilderService.build("mongo-write")).thenReturn(mongoWrite);
    when(mongoWrite.cipher(any(), eq("plain"))).thenReturn("out");

    ICipherService cipherService = configuration.cipherService(kmsClientBuilderService);

    assertEquals("out", cipherService.cipher(CipherType.ENCRYPT, "plain"));
  }

  @Test
  @DisplayName("cipherService cipher(DECRYPT) delegates to mongo-read")
  void cipherService_cipherDecrypt() {
    when(kmsClientBuilderService.build("mongo-read")).thenReturn(mongoRead);
    when(kmsClientBuilderService.build("mongo-write")).thenReturn(mongoWrite);
    when(mongoRead.cipher(any(), eq("blob"))).thenReturn("clear");

    ICipherService cipherService = configuration.cipherService(kmsClientBuilderService);

    assertEquals("clear", cipherService.cipher(CipherType.DECRYPT, "blob"));
  }

  @Test
  @DisplayName("stringHashingObjectMapper bean")
  void stringHashingObjectMapper() {
    StringHashingObjectMapper mapper = configuration.stringHashingObjectMapper();
    assertNotNull(mapper);
  }

  @Test
  @DisplayName("processInstanceCryptListener bean")
  void processInstanceCryptListener() {
    when(kmsClientBuilderService.build("mongo-read")).thenReturn(mongoRead);
    when(kmsClientBuilderService.build("mongo-write")).thenReturn(mongoWrite);
    ICipherService cipher = configuration.cipherService(kmsClientBuilderService);

    ProcessInstanceCryptListener listener =
        configuration.processInstanceCryptListener(
            cipher, configuration.stringHashingObjectMapper());
    assertNotNull(listener);
  }

  @Test
  @DisplayName("decisionInstanceCryptListener bean")
  void decisionInstanceCryptListener() {
    when(kmsClientBuilderService.build("mongo-read")).thenReturn(mongoRead);
    when(kmsClientBuilderService.build("mongo-write")).thenReturn(mongoWrite);

    DecisionInstanceCryptListener listener =
        configuration.decisionInstanceCryptListener(
            configuration.cipherService(kmsClientBuilderService));
    assertNotNull(listener);
  }
}
