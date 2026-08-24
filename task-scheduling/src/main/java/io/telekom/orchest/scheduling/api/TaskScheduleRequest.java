package io.telekom.orchest.scheduling.api;

import java.time.Instant;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Producer-side request to schedule a task.
 *
 * <p>The pair {@link #type} + {@link #businessKey} is the idempotency key — calling {@link
 * TaskScheduler#schedule(TaskScheduleRequest)} twice with the same pair updates the existing row
 * rather than creating a duplicate.
 */
@Data
@Builder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
public class TaskScheduleRequest {

  /** Task type — must match a registered {@link TaskHandler#type()}. */
  private String type;

  /**
   * Optional business key. If {@code null}, the scheduler treats every call as creating a new row
   * (no de-duplication).
   */
  private String businessKey;

  /** JSON-serialised payload. */
  private String payload;

  /** When the task should fire. {@code null} → "now". */
  private Instant triggerAt;

  /** Maximum execution attempts. {@code 0} or negative falls back to the scheduler default. */
  private int maxAttempts;
}
