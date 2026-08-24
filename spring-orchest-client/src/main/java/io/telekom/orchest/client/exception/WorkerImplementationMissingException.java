package io.telekom.orchest.client.exception;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

/** Thrown when a worker event arrives but no corresponding worker implementation is registered. */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class WorkerImplementationMissingException extends RuntimeException {
  /**
   * @param message description of the missing worker type
   */
  public WorkerImplementationMissingException(String message) {
    super(message);
  }
}
