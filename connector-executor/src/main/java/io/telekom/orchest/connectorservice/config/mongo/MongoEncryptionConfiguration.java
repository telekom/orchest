package io.telekom.orchest.connectorservice.config.mongo;

import de.telekom.solutions.kmsclient.CryptoService;
import de.telekom.solutions.kmsclient.EnableKMS;
import de.telekom.solutions.kmsclient.KMSClientBuilderService;
import io.telekom.orchest.adapter.mongo.config.StringHashingObjectMapper;
import io.telekom.orchest.adapter.mongo.listners.DecisionInstanceCryptListener;
import io.telekom.orchest.adapter.mongo.listners.ProcessInstanceCryptListener;
import io.telekom.orchest.api.core.adapters.cipher.CipherType;
import io.telekom.orchest.api.core.adapters.cipher.ICipherService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/** Configuration for MongoDB encryption/decryption using KMS for the connector service. */
@EnableKMS
@Configuration
public class MongoEncryptionConfiguration {

  /**
   * Creates a cipher service backed by separate KMS keys for read (decrypt) and write (encrypt).
   *
   * @param kmsClientBuilderService the KMS client builder service
   * @return an ICipherService implementation
   */
  @Bean
  public ICipherService cipherService(KMSClientBuilderService kmsClientBuilderService) {
    CryptoService mongoRead = kmsClientBuilderService.build("mongo-read");
    CryptoService mongoWrite = kmsClientBuilderService.build("mongo-write");
    return new ICipherService() {
      @Override
      public String cipher(CipherType type, String data) {
        return switch (type) {
          case ENCRYPT ->
              mongoWrite.cipher(de.telekom.solutions.kmsclient.api.CipherType.ENCRYPT, data);
          case DECRYPT ->
              mongoRead.cipher(de.telekom.solutions.kmsclient.api.CipherType.DECRYPT, data);
        };
      }

      @Override
      public String encrypt(String data) {
        return mongoWrite.encrypt(data);
      }

      @Override
      public String decrypt(String cipherText) {
        return mongoRead.decrypt(cipherText);
      }
    };
  }

  /**
   * Creates the object mapper used for hashing string fields before persistence.
   *
   * @return a StringHashingObjectMapper instance
   */
  @Bean
  public StringHashingObjectMapper stringHashingObjectMapper() {
    return new StringHashingObjectMapper();
  }

  /**
   * Creates the MongoDB lifecycle listener that encrypts/decrypts process instance variables.
   *
   * @param cipherService the cipher service for encryption
   * @param stringHashingObjectMapper the hashing mapper
   * @return a ProcessInstanceCryptListener
   */
  @Bean
  public ProcessInstanceCryptListener processInstanceCryptListener(
      ICipherService cipherService, StringHashingObjectMapper stringHashingObjectMapper) {
    return new ProcessInstanceCryptListener(cipherService, stringHashingObjectMapper);
  }

  /**
   * Creates the MongoDB lifecycle listener that encrypts/decrypts decision instance data.
   *
   * @param cipherService the cipher service for encryption
   * @return a DecisionInstanceCryptListener
   */
  @Bean
  public DecisionInstanceCryptListener decisionInstanceCryptListener(ICipherService cipherService) {
    return new DecisionInstanceCryptListener(cipherService);
  }
}
