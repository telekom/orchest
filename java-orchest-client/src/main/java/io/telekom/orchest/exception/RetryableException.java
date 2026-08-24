package io.telekom.orchest.exception;

import java.time.Duration;
import lombok.Getter;

/**
 * Exception indicating that a worker task has failed but should be retried. Can optionally specify
 * a backoff duration before retry.
 */
@Getter
public class RetryableException extends RuntimeException {

  private final Duration retryBackoff;

  /**
   * Creates a retryable exception with a specified backoff duration.
   *
   * @param message the error message
   * @param retryBackoff the duration to wait before retrying
   */
  public RetryableException(String message, Duration retryBackoff) {
    super(message);
    this.retryBackoff = retryBackoff;
  }

  /**
   * Creates a retryable exception with a cause and backoff duration.
   *
   * @param message the error message
   * @param cause the underlying cause
   * @param retryBackoff the duration to wait before retrying
   */
  public RetryableException(String message, Throwable cause, Duration retryBackoff) {
    super(message, cause);
    this.retryBackoff = retryBackoff;
  }

  /**
   * Creates a retryable exception with no backoff (immediate retry).
   *
   * @param message the error message
   */
  public RetryableException(String message) {
    super(message);
    this.retryBackoff = null;
  }

  /**
   * Creates a retryable exception with a cause and no backoff.
   *
   * @param message the error message
   * @param cause the underlying cause
   */
  public RetryableException(String message, Throwable cause) {
    super(message, cause);
    this.retryBackoff = null;
  }
}
