package io.telekom.orchest.scheduling.api;

import java.time.Instant;
import java.util.Optional;

/**
 * Producer-facing API for scheduling deferred work.
 *
 * <p>Implementations are required to be thread-safe and idempotent on {@code (type, businessKey)} —
 * repeated {@link #schedule(TaskScheduleRequest)} calls with the same pair update the existing row
 * instead of creating a duplicate.
 */
public interface TaskScheduler {

  /**
   * Schedule a task. If a task with the same {@code (type, businessKey)} already exists, its
   * trigger time, payload and attempt counter are reset.
   *
   * @return the persisted task (with store-generated {@code id} populated).
   */
  ScheduledTask schedule(TaskScheduleRequest request);

  /**
   * Convenience overload — schedules a task to fire at {@code triggerAt} with no business key (i.e.
   * always creates a new row).
   */
  default ScheduledTask schedule(String type, String payload, Instant triggerAt) {
    return schedule(
        TaskScheduleRequest.builder().type(type).payload(payload).triggerAt(triggerAt).build());
  }

  /**
   * Cancel a previously-scheduled task identified by its business key. Has no effect if no matching
   * task exists.
   */
  void cancelByBusinessKey(String type, String businessKey);

  /** Cancel by store id. No-op if the row no longer exists. */
  void cancelById(String id);

  /** Retrieve the current state of a task by id — primarily for diagnostics. */
  Optional<ScheduledTask> findById(String id);

  /** Retrieve a task by its idempotency key — primarily for diagnostics. */
  Optional<ScheduledTask> findByBusinessKey(String type, String businessKey);
}
