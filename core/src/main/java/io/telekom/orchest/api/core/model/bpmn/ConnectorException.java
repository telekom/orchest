package io.telekom.orchest.api.core.model.bpmn;

/**
 * Exception thrown when an error occurs during connector task execution. Used to wrap errors that
 * occur when executing BPMN connector tasks (e.g., REST connectors).
 */
public class ConnectorException extends RuntimeException {
  /**
   * Constructs a new ConnectorException with the specified message.
   *
   * @param message The detail message.
   */
  public ConnectorException(String message) {
    super(message);
  }

  /**
   * Constructs a new ConnectorException with the specified message and cause.
   *
   * @param message The detail message.
   * @param cause The cause of the exception.
   */
  public ConnectorException(String message, Throwable cause) {
    super(message, cause);
  }
}
