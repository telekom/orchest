package io.telekom.orchest.api.core.adapters.data.model;

/** Lifecycle states for a persistent telemetry alert (Grafana-style). */
public enum AlertState {
  /** Alert is actively firing and notifications are being sent. */
  FIRING,
  /** Alert has been acknowledged by an operator but not yet resolved. */
  ACKNOWLEDGED,
  /** Alert notifications are temporarily suppressed. */
  SILENCED,
  /** Alert condition has been resolved. */
  RESOLVED,
  /** Alert is administratively disabled. */
  DISABLED
}
