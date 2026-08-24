package io.telekom.orchest.cache.core;

/** Runtime exception thrown when a cache operation fails (e.g. loader error, refresh timeout). */
public class CacheException extends RuntimeException {

  /**
   * Creates a cache exception with a message and underlying cause.
   *
   * @param message descriptive error message
   * @param cause the underlying throwable
   */
  public CacheException(String message, Throwable cause) {
    super(message, cause);
  }

  /**
   * Creates a cache exception with a message only.
   *
   * @param message descriptive error message
   */
  public CacheException(String message) {
    super(message);
  }
}
