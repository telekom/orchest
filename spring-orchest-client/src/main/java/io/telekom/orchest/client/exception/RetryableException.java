package io.telekom.orchest.client.exception;

import java.time.Duration;
import lombok.Getter;

/**
 * Exception thrown by job workers to signal a transient failure that should be retried. Optionally
 * carries a {@link java.time.Duration} backoff hint for server-side retry scheduling.
 */
@Getter
public class RetryableException extends RuntimeException {

  private final Duration retryBackoff;

  /**
   * @param message the error message
   * @param retryBackoff the suggested backoff before the next retry attempt
   */
  public RetryableException(String message, Duration retryBackoff) {
    super(message);
    this.retryBackoff = retryBackoff;
  }

  /**
   * @param message the error message
   * @param cause the underlying cause
   * @param retryBackoff the suggested backoff before the next retry attempt
   */
  public RetryableException(String message, Throwable cause, Duration retryBackoff) {
    super(message, cause);
    this.retryBackoff = retryBackoff;
  }

  /**
   * Creates a retryable exception with no backoff hint (client-side immediate retry).
   *
   * @param message the error message
   */
  public RetryableException(String message) {
    super(message);
    this.retryBackoff = null;
  }

  /**
   * Creates a retryable exception with no backoff hint (client-side immediate retry).
   *
   * @param message the error message
   * @param cause the underlying cause
   */
  public RetryableException(String message, Throwable cause) {
    super(message, cause);
    this.retryBackoff = null;
  }
}
