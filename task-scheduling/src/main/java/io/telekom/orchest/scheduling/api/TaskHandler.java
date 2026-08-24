package io.telekom.orchest.scheduling.api;

/**
 * Consumer-side hook invoked by the dispatcher when a task fires.
 *
 * <p>Implementations are looked up by {@link #type()} — a single {@link TaskHandler} per type is
 * registered per JVM. Throwing any exception triggers the standard retry/back-off path; returning
 * normally marks the task complete.
 *
 * <p>Handlers <strong>must be idempotent</strong>. The dispatcher delivers at-least-once: a crashed
 * worker mid-execution will have its lease re-claimed by another dispatcher and the task replayed.
 */
@FunctionalInterface
public interface TaskHandler {

  /**
   * Logical task type this handler serves. Must match {@link TaskScheduleRequest#getType()}
   * exactly.
   */
  default String type() {
    throw new UnsupportedOperationException(
        "Override type() or register the handler with an explicit type");
  }

  /** Execute the task. Throw to trigger a retry; return normally to mark the task complete. */
  void handle(ScheduledTask task) throws Exception;
}
