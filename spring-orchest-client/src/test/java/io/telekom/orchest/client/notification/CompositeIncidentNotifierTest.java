package io.telekom.orchest.client.notification;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

/** Tests for {@link CompositeIncidentNotifier} dispatch, enablement, and error isolation. */
@ExtendWith(MockitoExtension.class)
class CompositeIncidentNotifierTest {

  @Mock private IncidentNotifier notifier1;

  @Mock private IncidentNotifier notifier2;

  @Mock private IncidentNotifier notifier3;

  private final IncidentNotification sampleNotification =
      new IncidentNotification("pi-123", "corr-456", "TEST", "Something went wrong");

  // ============================================================
  // notify with multiple enabled notifiers
  // ============================================================

  @Test
  @DisplayName("notify calls all enabled notifiers")
  void notify_multipleEnabledNotifiers_allCalled() {
    when(notifier1.isEnabled()).thenReturn(true);
    when(notifier2.isEnabled()).thenReturn(true);

    CompositeIncidentNotifier composite =
        new CompositeIncidentNotifier(List.of(notifier1, notifier2));

    composite.notify(sampleNotification);

    verify(notifier1).notify(sampleNotification);
    verify(notifier2).notify(sampleNotification);
  }

  // ============================================================
  // notify with disabled notifier
  // ============================================================

  @Test
  @DisplayName("notify skips disabled notifiers")
  void notify_disabledNotifier_skipped() {
    when(notifier1.isEnabled()).thenReturn(true);
    when(notifier2.isEnabled()).thenReturn(false);

    CompositeIncidentNotifier composite =
        new CompositeIncidentNotifier(List.of(notifier1, notifier2));

    composite.notify(sampleNotification);

    verify(notifier1).notify(sampleNotification);
    verify(notifier2, never()).notify(any());
  }

  // ============================================================
  // notify with one failing notifier - error isolation
  // ============================================================

  @Test
  @DisplayName("notify continues to other notifiers even when one fails")
  void notify_oneNotifierFails_otherStillCalled() {
    when(notifier1.isEnabled()).thenReturn(true);
    when(notifier2.isEnabled()).thenReturn(true);
    when(notifier3.isEnabled()).thenReturn(true);
    doThrow(new RuntimeException("notification channel down")).when(notifier1).notify(any());

    CompositeIncidentNotifier composite =
        new CompositeIncidentNotifier(List.of(notifier1, notifier2, notifier3));

    // Should not throw
    assertDoesNotThrow(() -> composite.notify(sampleNotification));

    verify(notifier1).notify(sampleNotification);
    verify(notifier2).notify(sampleNotification);
    verify(notifier3).notify(sampleNotification);
  }

  // ============================================================
  // isEnabled returns true if any notifier is enabled
  // ============================================================

  @Test
  @DisplayName("isEnabled returns true when at least one notifier is enabled")
  void isEnabled_oneEnabled_returnsTrue() {
    when(notifier1.isEnabled()).thenReturn(false);
    when(notifier2.isEnabled()).thenReturn(true);

    CompositeIncidentNotifier composite =
        new CompositeIncidentNotifier(List.of(notifier1, notifier2));

    assertTrue(composite.isEnabled());
  }

  @Test
  @DisplayName("isEnabled returns false when all notifiers are disabled")
  void isEnabled_allDisabled_returnsFalse() {
    when(notifier1.isEnabled()).thenReturn(false);
    when(notifier2.isEnabled()).thenReturn(false);

    CompositeIncidentNotifier composite =
        new CompositeIncidentNotifier(List.of(notifier1, notifier2));

    assertFalse(composite.isEnabled());
  }

  @Test
  @DisplayName("isEnabled returns false for empty notifier list")
  void isEnabled_emptyList_returnsFalse() {
    CompositeIncidentNotifier composite = new CompositeIncidentNotifier(List.of());

    assertFalse(composite.isEnabled());
  }
}
