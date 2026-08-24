package io.telekom.orchest.api.core.adapters.cipher;

/**
 * Interface for encryption and decryption services. Allows for pluggable security implementations.
 */
public interface ICipherService {

  /**
   * Processes data based on the specified cipher type (encrypt/decrypt).
   *
   * @param type The operation type (ENCRYPT/DECRYPT).
   * @param data The data to process.
   * @return The processed string.
   */
  String cipher(CipherType type, String data);

  /**
   * Encrypts the provided data.
   *
   * @param data The plaintext data.
   * @return The encrypted ciphertext.
   */
  String encrypt(String data);

  /**
   * Decrypts the provided ciphertext.
   *
   * @param cipherText The encrypted data.
   * @return The decrypted plaintext.
   */
  String decrypt(String cipherText);
}
