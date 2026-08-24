package io.telekom.orchest.adapter.kafka.client;

import static io.telekom.orchest.adapter.kafka.client.KafkaConstant.ORCHEST_KAFKA_CRYPTO_SERVICE_BEAN_NAME;

import de.telekom.solutions.kmsclient.CryptoService;
import de.telekom.solutions.kmsclient.api.CipherType;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

/** Default encryption client that delegates to the platform KMS CryptoService. */
@Component
public class KafkaEncryptionClient implements IEncryptionClient {

  private final CryptoService cryptoService;

  public KafkaEncryptionClient(
      @Qualifier(ORCHEST_KAFKA_CRYPTO_SERVICE_BEAN_NAME) CryptoService cryptoService) {
    this.cryptoService = cryptoService;
  }

  @Override
  public String encrypt(String data) {
    return cryptoService.cipher(CipherType.ENCRYPT, data);
  }

  @Override
  public String decrypt(String data) {
    return cryptoService.cipher(CipherType.DECRYPT, data);
  }

  @Override
  public boolean isEncryptionEnabled() {
    return cryptoService.isEnabled();
  }
}
