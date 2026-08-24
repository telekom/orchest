package io.telekom.orchest.scheduling.runtime;

import io.telekom.orchest.scheduling.api.ScheduledTask;
import io.telekom.orchest.scheduling.api.TaskScheduleRequest;
import io.telekom.orchest.scheduling.api.TaskScheduler;
import io.telekom.orchest.scheduling.spi.ScheduledTaskStore;
import java.time.Clock;
import java.time.Instant;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Default {@link TaskScheduler} implementation — a thin facade over a {@link ScheduledTaskStore}.
 *
 * <p>Producer-side concerns only: enqueue, cancel, look up. Polling, leasing and dispatching live
 * in {@link TaskDispatcher}. The two are deliberately split so producer-only services (e.g. {@code
 * orchest-engine}) can depend on the scheduler without paying the cost of running dispatcher
 * threads.
 */
@Slf4j
@RequiredArgsConstructor
public class DefaultTaskScheduler implements TaskScheduler {

  private final ScheduledTaskStore store;
  private final SchedulerRuntimeProperties properties;
  private final Clock clock;

  @Override
  public ScheduledTask schedule(TaskScheduleRequest request) {
    if (request.getType() == null || request.getType().isBlank()) {
      throw new IllegalArgumentException("TaskScheduleRequest.type is required");
    }
    TaskScheduleRequest normalised = normalise(request);
    ScheduledTask task =
        (normalised.getBusinessKey() == null)
            ? store.insert(normalised)
            : store.upsertByBusinessKey(normalised);
    log.debug(
        "Scheduled task type='{}' businessKey='{}' triggerAt={}",
        task.getType(),
        task.getBusinessKey(),
        task.getTriggerAt());
    return task;
  }

  @Override
  public void cancelByBusinessKey(String type, String businessKey) {
    if (type == null || businessKey == null) {
      return;
    }
    store.deleteByBusinessKey(type, businessKey);
  }

  @Override
  public void cancelById(String id) {
    if (id == null) {
      return;
    }
    store.deleteById(id);
  }

  @Override
  public Optional<ScheduledTask> findById(String id) {
    return store.findById(id);
  }

  @Override
  public Optional<ScheduledTask> findByBusinessKey(String type, String businessKey) {
    return store.findByBusinessKey(type, businessKey);
  }

  private TaskScheduleRequest normalise(TaskScheduleRequest request) {
    Instant triggerAt =
        request.getTriggerAt() != null ? request.getTriggerAt() : Instant.now(clock);
    int maxAttempts =
        request.getMaxAttempts() > 0
            ? request.getMaxAttempts()
            : properties.getDefaultMaxAttempts();
    return TaskScheduleRequest.builder()
        .type(request.getType())
        .businessKey(request.getBusinessKey())
        .payload(request.getPayload())
        .triggerAt(triggerAt)
        .maxAttempts(maxAttempts)
        .build();
  }
}
