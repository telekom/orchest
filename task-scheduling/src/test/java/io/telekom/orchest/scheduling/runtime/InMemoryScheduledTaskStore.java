package io.telekom.orchest.scheduling.runtime;

import io.telekom.orchest.scheduling.api.ScheduledTask;
import io.telekom.orchest.scheduling.api.TaskScheduleRequest;
import io.telekom.orchest.scheduling.api.TaskState;
import io.telekom.orchest.scheduling.spi.ScheduledTaskStore;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Test-only in-memory implementation of {@link ScheduledTaskStore}. Mirrors the atomicity
 * guarantees of a real backend by serialising all mutations behind a single lock. Used by the
 * dispatcher tests so they don't pull in a Mongo dependency.
 */
public final class InMemoryScheduledTaskStore implements ScheduledTaskStore {

  private final ConcurrentMap<String, ScheduledTask> tasks = new ConcurrentHashMap<>();
  private final ReentrantLock claimLock = new ReentrantLock();
  private final Clock clock;

  public InMemoryScheduledTaskStore(Clock clock) {
    this.clock = clock;
  }

  @Override
  public ScheduledTask insert(TaskScheduleRequest request) {
    Instant now = Instant.now(clock);
    String id = UUID.randomUUID().toString();
    ScheduledTask task =
        ScheduledTask.builder()
            .id(id)
            .type(request.getType())
            .businessKey(request.getBusinessKey())
            .payload(request.getPayload())
            .triggerAt(request.getTriggerAt())
            .state(TaskState.PENDING)
            .attempts(0)
            .maxAttempts(request.getMaxAttempts())
            .version(0)
            .createdAt(now)
            .updatedAt(now)
            .build();
    tasks.put(id, task);
    return task;
  }

  @Override
  public ScheduledTask upsertByBusinessKey(TaskScheduleRequest request) {
    if (request.getBusinessKey() == null) {
      return insert(request);
    }
    synchronized (tasks) {
      Optional<ScheduledTask> existing =
          tasks.values().stream()
              .filter(
                  t ->
                      request.getType().equals(t.getType())
                          && request.getBusinessKey().equals(t.getBusinessKey()))
              .findFirst();
      if (existing.isEmpty()) {
        return insert(request);
      }
      ScheduledTask t = existing.get();
      ScheduledTask updated =
          t.toBuilder()
              .payload(request.getPayload())
              .triggerAt(request.getTriggerAt())
              .state(TaskState.PENDING)
              .attempts(0)
              .maxAttempts(request.getMaxAttempts())
              .ownerId(null)
              .leaseUntil(null)
              .lastError(null)
              .version(t.getVersion() + 1)
              .updatedAt(Instant.now(clock))
              .build();
      tasks.put(t.getId(), updated);
      return updated;
    }
  }

  @Override
  public List<ScheduledTask> claimDue(
      String ownerId, Instant now, Duration leaseDuration, int batchSize) {
    if (batchSize <= 0) {
      return List.of();
    }
    claimLock.lock();
    try {
      List<ScheduledTask> claimed = new ArrayList<>();
      tasks.values().stream()
          .filter(t -> isDue(t, now))
          .sorted(Comparator.comparing(ScheduledTask::getTriggerAt))
          .limit(batchSize)
          .forEach(
              t -> {
                ScheduledTask leased =
                    t.toBuilder()
                        .state(TaskState.RUNNING)
                        .ownerId(ownerId)
                        .leaseUntil(now.plus(leaseDuration))
                        .attempts(t.getAttempts() + 1)
                        .updatedAt(now)
                        .version(t.getVersion() + 1)
                        .build();
                tasks.put(leased.getId(), leased);
                claimed.add(leased);
              });
      return claimed;
    } finally {
      claimLock.unlock();
    }
  }

  private boolean isDue(ScheduledTask t, Instant now) {
    if (t.getState() == TaskState.PENDING && !t.getTriggerAt().isAfter(now)) {
      return true;
    }
    return t.getState() == TaskState.RUNNING
        && t.getLeaseUntil() != null
        && t.getLeaseUntil().isBefore(now);
  }

  @Override
  public void completeAndDelete(String id, long expectedVersion) {
    ScheduledTask current = tasks.get(id);
    if (current == null || current.getVersion() != expectedVersion) {
      return;
    }
    tasks.remove(id);
  }

  @Override
  public void fail(String id, long expectedVersion, Instant nextTriggerAt, String lastError) {
    ScheduledTask current = tasks.get(id);
    if (current == null || current.getVersion() != expectedVersion) {
      return;
    }
    ScheduledTask updated =
        current.toBuilder()
            .state(TaskState.PENDING)
            .triggerAt(nextTriggerAt)
            .ownerId(null)
            .leaseUntil(null)
            .lastError(lastError)
            .version(current.getVersion() + 1)
            .updatedAt(Instant.now(clock))
            .build();
    tasks.put(id, updated);
  }

  @Override
  public void markDead(String id, long expectedVersion, String lastError) {
    ScheduledTask current = tasks.get(id);
    if (current == null || current.getVersion() != expectedVersion) {
      return;
    }
    ScheduledTask updated =
        current.toBuilder()
            .state(TaskState.DEAD)
            .ownerId(null)
            .leaseUntil(null)
            .lastError(lastError)
            .version(current.getVersion() + 1)
            .updatedAt(Instant.now(clock))
            .build();
    tasks.put(id, updated);
  }

  @Override
  public void deleteByBusinessKey(String type, String businessKey) {
    tasks
        .values()
        .removeIf(t -> type.equals(t.getType()) && businessKey.equals(t.getBusinessKey()));
  }

  @Override
  public void deleteById(String id) {
    tasks.remove(id);
  }

  @Override
  public Optional<ScheduledTask> findById(String id) {
    return Optional.ofNullable(tasks.get(id));
  }

  @Override
  public Optional<ScheduledTask> findByBusinessKey(String type, String businessKey) {
    return tasks.values().stream()
        .filter(t -> type.equals(t.getType()) && businessKey.equals(t.getBusinessKey()))
        .findFirst();
  }

  /**
   * Returns the number of tasks currently in the store.
   *
   * @return current task count
   */
  public int size() {
    return tasks.size();
  }
}
