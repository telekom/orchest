package io.telekom.orchest.scheduling.api;

import java.time.Instant;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * A unit of work persisted in a {@link io.telekom.orchest.scheduling.spi.ScheduledTaskStore}.
 *
 * <p>Identity is the tuple {@code (type, businessKey)}; the store enforces uniqueness on this pair
 * so that producers can call {@code schedule()} idempotently. The {@code id} is store-generated and
 * is the primary key used for atomic claim/complete operations.
 *
 * <p>Payloads are opaque JSON strings — keeping the store schema-free lets adopters add new task
 * types without touching the persistence layer or running migrations.
 */
@Data
@Builder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
public class ScheduledTask {

  /** Store-generated primary key (e.g. ObjectId, UUID, sequence). */
  private String id;

  /** Logical task type — the dispatcher routes by this to a {@link TaskHandler}. */
  private String type;

  /**
   * Optional business key that — together with {@link #type} — uniquely identifies the task. Used
   * for idempotent scheduling and cancellation.
   */
  private String businessKey;

  /** Opaque JSON payload supplied by the producer. */
  private String payload;

  /** When the task should next become eligible to run. */
  private Instant triggerAt;

  /** Current lifecycle state. */
  private TaskState state;

  /** How many times execution has been attempted. */
  private int attempts;

  /** Maximum allowed attempts before transitioning to {@link TaskState#DEAD}. */
  private int maxAttempts;

  /** Owner id of the dispatcher currently leasing this task ({@code null} when {@code PENDING}). */
  private String ownerId;

  /** Lease deadline. After this instant the task can be re-claimed by any dispatcher. */
  private Instant leaseUntil;

  /** Last failure message — useful for forensics on {@code DEAD} rows. */
  private String lastError;

  /** Optimistic concurrency token. The store bumps it on every state change. */
  private long version;

  /** When the task was first persisted. */
  private Instant createdAt;

  /** Last time the task row was updated. */
  private Instant updatedAt;
}
