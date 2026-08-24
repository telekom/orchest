package io.telekom.orchest.kafka.kms;

import io.telekom.orchest.api.core.adapters.cipher.CipherType;

/**
 * Interface for encryption and decryption operations using AWS KMS or other encryption services.
 * Provides methods to encrypt/decrypt data and check if encryption is enabled.
 */
public interface IEncryptionClient {

  /**
   * Performs encryption or decryption based on the specified cipher type.
   *
   * @param type The operation type (ENCRYPT or DECRYPT).
   * @param data The data to process.
   * @return The encrypted or decrypted string.
   */
  String cipher(CipherType type, String data);

  /**
   * Encrypts the provided plaintext data.
   *
   * @param plainText The plaintext data to encrypt.
   * @return The encrypted ciphertext.
   */
  String encrypt(String plainText);

  /**
   * Decrypts the provided ciphertext data.
   *
   * @param cipherText The encrypted data to decrypt.
   * @return The decrypted plaintext.
   */
  String decrypt(String cipherText);

  /**
   * Checks if encryption is currently enabled.
   *
   * @return true if encryption is enabled, false otherwise.
   */
  boolean isEncryptionEnabled();
}
