package io.telekom.orchest.alerting;

import io.telekom.orchest.alerting.api.dto.AlertDTO;

/**
 * Interface for alerting services. Implementations provide mechanisms to send alerts (e.g., via
 * Email, MS Teams) when critical events occur.
 */
public interface IAlertingService {

  /**
   * Sends an alert synchronously.
   *
   * @param alert The alert data transfer object.
   * @return true if sent successfully, false otherwise.
   */
  boolean sendAlert(AlertDTO alert);

  /**
   * Sends an alert asynchronously.
   *
   * @param alert The alert data transfer object.
   */
  void sendAlertAsync(AlertDTO alert);

  /**
   * Checks if the alerting service is active/enabled.
   *
   * @return true if enabled.
   */
  boolean isActive();
}
