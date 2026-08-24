package io.telekom.orchest.client.notification;

import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Dispatches incident notifications to all enabled {@link IncidentNotifier} instances. Failures in
 * one notifier do not prevent delivery to subsequent notifiers.
 */
@Slf4j
@RequiredArgsConstructor
public class CompositeIncidentNotifier implements IncidentNotifier {
  private final List<IncidentNotifier> notifiers;

  /** {@inheritDoc} */
  @Override
  public void notify(IncidentNotification notification) {
    for (IncidentNotifier notifier : notifiers) {
      if (notifier.isEnabled()) {
        try {
          notifier.notify(notification);
        } catch (Exception e) {
          log.error(
              "Failed to send incident notification via {}: {}",
              notifier.getClass().getSimpleName(),
              e.getMessage(),
              e);
        }
      }
    }
  }

  /** {@inheritDoc} */
  @Override
  public boolean isEnabled() {
    return notifiers.stream().anyMatch(IncidentNotifier::isEnabled);
  }
}
