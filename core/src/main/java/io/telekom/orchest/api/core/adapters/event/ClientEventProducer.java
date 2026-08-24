package io.telekom.orchest.api.core.adapters.event;

/**
 * Interface for producing client worker events. Implementations are responsible for sending events
 * to client workers (e.g., via Kafka).
 *
 * @param <T> The type of event to produce.
 */
public interface ClientEventProducer<T> {

  /**
   * Sends a client worker event.
   *
   * @param event The event to send.
   */
  void sendClientWorkerEvent(T event);
}
