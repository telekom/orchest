package io.telekom.orchest.adapter.kafka.client;

/** Abstraction for encrypting and decrypting Kafka message payloads. */
public interface IEncryptionClient {

  /**
   * Encrypts a plaintext string.
   *
   * @param plainText the text to encrypt
   * @return the encrypted ciphertext
   */
  String encrypt(String plainText);

  /**
   * Decrypts a ciphertext string back to plaintext.
   *
   * @param cipherText the encrypted text to decrypt
   * @return the decrypted plaintext
   */
  String decrypt(String cipherText);

  /**
   * Indicates whether encryption is enabled in the current environment.
   *
   * @return true if encryption is active
   */
  boolean isEncryptionEnabled();
}
