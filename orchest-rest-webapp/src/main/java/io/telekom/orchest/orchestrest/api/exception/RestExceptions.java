package io.telekom.orchest.orchestrest.api.exception;

import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * Custom exception class for REST API errors. Encapsulates an error message, a specific error code,
 * and optional details. Used to provide meaningful error responses to API clients.
 */
@Getter
@NoArgsConstructor
public class RestExceptions extends RuntimeException {

  private String message;
  private int code;
  private String details;

  /**
   * Constructs a new RestExceptions with the specified detail message.
   *
   * @param message The detail message.
   */
  public RestExceptions(String message) {
    super(message);
    this.message = message;
  }

  /**
   * Constructs a new RestExceptions with the specified detail message and error code.
   *
   * @param message The detail message.
   * @param code The specific error code.
   */
  public RestExceptions(String message, int code) {
    super(message);
    this.message = message;
    this.code = code;
  }

  /**
   * Constructs a new RestExceptions with the specified detail message, error code, and additional
   * details.
   *
   * @param message The detail message.
   * @param code The specific error code.
   * @param details Additional error details.
   */
  public RestExceptions(String message, int code, String details) {
    super(message);
    this.message = message;
    this.code = code;
    this.details = details;
  }
}
