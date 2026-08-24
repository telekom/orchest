package io.telekom.orchest.config.mongo;

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

/**
 * Configuration for MongoDB encryption/decryption using KMS. Sets up the {@link ICipherService}
 * bean to handle data encryption for sensitive fields stored in MongoDB.
 */
@EnableKMS
@Configuration
public class MongoEncryptionConfiguration {

  /**
   * Configures the CipherService with KMS clients for read and write operations.
   *
   * @param kmsClientBuilderService The KMS client builder.
   * @return A configured {@link ICipherService} implementation.
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
   * Creates a {@link StringHashingObjectMapper} bean for hashing sensitive string fields.
   *
   * @return a new {@link StringHashingObjectMapper} instance
   */
  @Bean
  public StringHashingObjectMapper stringHashingObjectMapper() {
    return new StringHashingObjectMapper();
  }

  /**
   * Creates a Mongo lifecycle listener that encrypts/decrypts process instance documents.
   *
   * @param cipherService the cipher service for encryption operations
   * @param stringHashingObjectMapper the object mapper for hashing strings
   * @return a new {@link ProcessInstanceCryptListener} instance
   */
  @Bean
  public ProcessInstanceCryptListener processInstanceCryptListener(
      ICipherService cipherService, StringHashingObjectMapper stringHashingObjectMapper) {
    return new ProcessInstanceCryptListener(cipherService, stringHashingObjectMapper);
  }

  /**
   * Creates a Mongo lifecycle listener that encrypts/decrypts decision instance documents.
   *
   * @param cipherService the cipher service for encryption operations
   * @return a new {@link DecisionInstanceCryptListener} instance
   */
  @Bean
  public DecisionInstanceCryptListener decisionInstanceCryptListener(ICipherService cipherService) {
    return new DecisionInstanceCryptListener(cipherService);
  }
}
