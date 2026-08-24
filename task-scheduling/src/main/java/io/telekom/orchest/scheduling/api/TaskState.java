package io.telekom.orchest.scheduling.api;

/**
 * Lifecycle state of a {@link ScheduledTask}.
 *
 * <p>State transitions:
 *
 * <pre>
 *   PENDING ─claim─▶ RUNNING ─complete─▶ (deleted)
 *      ▲                │
 *      │                └─fail (attempts &lt; max)─▶ PENDING (rescheduled)
 *      │                │
 *      │                └─fail (attempts == max)─▶ DEAD
 *      │
 *   recover-stale-lease
 * </pre>
 */
public enum TaskState {
  /** Waiting for its scheduled trigger time and to be picked up by a dispatcher. */
  PENDING,
  /** Claimed by a dispatcher and currently being executed. */
  RUNNING,
  /** All attempts exhausted; left in the store for inspection (a dead-letter row). */
  DEAD
}
