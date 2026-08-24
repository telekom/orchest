package io.telekom.orchest.scheduling.spi;

import io.telekom.orchest.scheduling.api.ScheduledTask;
import io.telekom.orchest.scheduling.api.TaskScheduleRequest;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Optional;

/**
 * Storage SPI for the task scheduler.
 *
 * <p>The scheduler depends only on this contract — adopters plug in MongoDB, PostgreSQL, MySQL,
 * DynamoDB, etc. by providing a single implementation. The interface is intentionally narrow and
 * built around <strong>atomic single-document operations</strong> so it can be implemented on any
 * backend that supports compare-and-set on a row, including stores without multi-document
 * transactions.
 *
 * <p>Distributed-correctness contract — implementations <em>must</em> guarantee:
 *
 * <ul>
 *   <li>{@link #upsertByBusinessKey(TaskScheduleRequest)} is atomic on {@code (type, businessKey)}
 *       (one winner; no duplicate rows).
 *   <li>{@link #claimDue(String, Instant, Duration, int)} hands a given task to <em>at most
 *       one</em> caller per lease window. The implementation should use an atomic find-and-modify /
 *       SELECT … FOR UPDATE SKIP LOCKED equivalent on each row, in {@code triggerAt ASC} order,
 *       filtered by {@code state == PENDING AND triggerAt &le; now} <em>or</em> a stale lease
 *       ({@code state == RUNNING AND leaseUntil &lt; now}).
 *   <li>{@link #completeAndDelete(String, long)}, {@link #fail(String, long, Instant, String)} and
 *       {@link #markDead(String, long, String)} are no-ops when the {@code expectedVersion} does
 *       not match (lease was stolen) — they must <strong>not</strong> overwrite a newer state.
 * </ul>
 *
 * <p>Lease semantics — when a worker claims a task, the store stamps {@code ownerId} and bumps
 * {@code leaseUntil} to {@code now + leaseDuration}. The worker must call {@code complete} or
 * {@code fail} before {@code leaseUntil} elapses; if it crashes, another dispatcher will see a
 * stale lease and re-claim the row on its next poll.
 */
public interface ScheduledTaskStore {

  /** Insert a new task. Used when no business key is supplied (always creates a new row). */
  ScheduledTask insert(TaskScheduleRequest request);

  /**
   * Insert-or-update keyed by {@code (type, businessKey)}. Atomic on the pair.
   *
   * <p>If an existing row is found, its {@code triggerAt}, {@code payload}, {@code state} and
   * {@code attempts} are reset; {@code id} is preserved.
   */
  ScheduledTask upsertByBusinessKey(TaskScheduleRequest request);

  /**
   * Atomically claim up to {@code batchSize} due tasks for the given owner. A task is "due" if:
   *
   * <ul>
   *   <li>{@code state == PENDING && triggerAt <= now}; or
   *   <li>{@code state == RUNNING && leaseUntil < now} (stale lease — original worker died).
   * </ul>
   *
   * <p>Each returned task has its {@code state} flipped to {@code RUNNING}, {@code ownerId} set to
   * {@code ownerId}, and {@code leaseUntil} set to {@code now + leaseDuration}.
   */
  List<ScheduledTask> claimDue(String ownerId, Instant now, Duration leaseDuration, int batchSize);

  /** Mark the task complete and delete the row. No-op if {@code expectedVersion} mismatches. */
  void completeAndDelete(String id, long expectedVersion);

  /**
   * Reset a task back to {@code PENDING} for retry at {@code nextTriggerAt}, recording the last
   * error for forensics. No-op if {@code expectedVersion} mismatches.
   */
  void fail(String id, long expectedVersion, Instant nextTriggerAt, String lastError);

  /** Move a task to {@code DEAD} after attempts are exhausted. No-op on version mismatch. */
  void markDead(String id, long expectedVersion, String lastError);

  /**
   * Cancel a task by business key. Implemented as a delete on rows matching {@code (type,
   * businessKey)}. No-op if no row matches.
   */
  void deleteByBusinessKey(String type, String businessKey);

  /** Cancel a task by id. No-op if the row no longer exists. */
  void deleteById(String id);

  /** Diagnostics. */
  Optional<ScheduledTask> findById(String id);

  /** Diagnostics. */
  Optional<ScheduledTask> findByBusinessKey(String type, String businessKey);
}
