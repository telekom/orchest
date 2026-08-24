package io.telekom.orchest.client.notification;

/** Strategy interface for sending incident alerts to external channels (Teams, email, etc.). */
public interface IncidentNotifier {
  /**
   * Sends an incident notification to the configured channel.
   *
   * @param notification the incident details
   */
  void notify(IncidentNotification notification);

  /**
   * Returns whether this notifier is currently active.
   *
   * @return true if notifications will be delivered
   */
  boolean isEnabled();
}
