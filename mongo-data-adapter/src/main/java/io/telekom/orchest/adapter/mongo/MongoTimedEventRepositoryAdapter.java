package io.telekom.orchest.adapter.mongo;

import io.telekom.orchest.adapter.mongo.mapper.TimedEventMapper;
import io.telekom.orchest.adapter.mongo.repository.MongoTimedEventRepository;
import io.telekom.orchest.api.core.adapters.data.model.TimedEvent;
import io.telekom.orchest.api.core.adapters.data.repository.TimedEventRepository;
import io.telekom.orchest.scheduling.api.ScheduledTask;
import io.telekom.orchest.scheduling.api.TaskScheduleRequest;
import io.telekom.orchest.scheduling.api.TaskScheduler;
import java.time.Instant;
import java.time.ZoneId;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;

/**
 * MongoDB implementation of {@link TimedEventRepository}.
 *
 * <p>Persists the BPMN-domain {@link TimedEvent} document and registers a sibling task in the
 * distributed {@link TaskScheduler} so the dispatcher fires the timer when due. The previous design
 * used a TTL index plus a Mongo change stream — that has been replaced with the lease-based
 * scheduler so the firing path no longer depends on Mongo's TTL monitor (which has up-to-60-second
 * imprecision and only works on replica sets).
 *
 * <p>The {@code timedEventRegistryId} field on {@code TimedEvent} now holds the scheduler task id
 * rather than a TTL-collection id; that's how the dispatcher's task handler in {@code sentinel}
 * looks up the BPMN payload when the timer fires.
 */
@Slf4j
@Repository
@RequiredArgsConstructor
public class MongoTimedEventRepositoryAdapter implements TimedEventRepository {

  /**
   * Task type registered by sentinel for timer firings (intermediate catch and start timer). Keep
   * this constant in sync with the handler registration on the consumer side.
   */
  public static final String TIMER_FIRE_TASK_TYPE = "orchest.timer.fire";

  /** Task type registered by sentinel for progressive retries. */
  public static final String TASK_RETRY_TASK_TYPE = "orchest.task.retry";

  private final MongoTimedEventRepository mongoTimedEventRepository;
  private final TimedEventMapper timedEventMapper;
  private final TaskScheduler taskScheduler;

  /**
   * Registers a timed event by persisting it and scheduling a task for its trigger time.
   *
   * @param timedEvent the timed event to register
   * @return the persisted timed event with scheduler task ID stamped
   */
  @Override
  public TimedEvent registerEvent(TimedEvent timedEvent) {
    // Persist the BPMN domain row first so the scheduler payload can reference it by id.
    io.telekom.orchest.adapter.mongo.model.TimedEvent persisted =
        mongoTimedEventRepository.save(timedEventMapper.toDocument(timedEvent));
    Instant triggerAt =
        persisted.getTriggerAt() == null
            ? Instant.now()
            : persisted.getTriggerAt().atZone(ZoneId.systemDefault()).toInstant();

    ScheduledTask scheduledTask =
        taskScheduler.schedule(
            TaskScheduleRequest.builder()
                .type(taskTypeFor(persisted.getType()))
                // The BPMN row id is the natural idempotency key — registering the same TimedEvent
                // twice (e.g. a retried Kafka command) updates the existing scheduler row.
                .businessKey(persisted.getId())
                .payload(persisted.getId())
                .triggerAt(triggerAt)
                .build());

    // Stamp the scheduler task id back onto the BPMN row so the consumer can correlate.
    persisted.setTimedEventRegistryId(scheduledTask.getId());
    io.telekom.orchest.adapter.mongo.model.TimedEvent saved =
        mongoTimedEventRepository.save(persisted);
    log.debug(
        "Registered timer event id={} type={} taskId={} triggerAt={}",
        saved.getId(),
        saved.getType(),
        scheduledTask.getId(),
        triggerAt);
    return timedEventMapper.toDomain(saved);
  }

  /**
   * Finds a timed event by its scheduler registry ID.
   *
   * @param registryId the scheduler task ID
   * @return the matching timed event, or empty if not found
   */
  @Override
  public Optional<TimedEvent> findByTimedEventRegistryId(String registryId) {
    return mongoTimedEventRepository
        .findByTimedEventRegistryId(registryId)
        .map(timedEventMapper::toDomain);
  }

  /**
   * Finds a timed event by its document ID.
   *
   * @param id the document ID
   * @return the matching timed event, or empty if not found
   */
  @Override
  public Optional<TimedEvent> findById(String id) {
    return mongoTimedEventRepository.findById(id).map(timedEventMapper::toDomain);
  }

  /**
   * Finds the timer start event for a given process definition.
   *
   * @param processDefinitionId the process definition ID
   * @return the timer start event, or empty if none exists
   */
  @Override
  public Optional<TimedEvent> findStartTimedEvent(String processDefinitionId) {
    return mongoTimedEventRepository
        .findByProcessDefinitionIdAndIsStartEvent(processDefinitionId, true)
        .map(timedEventMapper::toDomain);
  }

  /**
   * Checks whether a duplicate task retry timer already exists for the given instance and activity.
   *
   * @param processInstanceId the process instance ID
   * @param activityId the activity ID
   * @return the existing duplicate event, or empty if none exists
   */
  @Override
  public Optional<TimedEvent> isDuplicateEvent(String processInstanceId, String activityId) {
    return mongoTimedEventRepository
        .isDuplicateTaskRetryEvent(processInstanceId, activityId)
        .map(timedEventMapper::toDomain);
  }

  /**
   * Deletes all timed events linked to the given event ID and cancels their scheduled tasks.
   *
   * @param linkedEventId the linked event ID whose timers should be removed
   */
  @Override
  public void deleteByLinkedEventId(String linkedEventId) {
    cancelScheduledFor(mongoTimedEventRepository.findByLinkedEventId(linkedEventId));
    mongoTimedEventRepository.deleteByLinkedEventId(linkedEventId);
  }

  /**
   * Removes a specific timed event and cancels its scheduled task.
   *
   * @param timedEvent the timed event to remove
   */
  @Override
  public void remove(TimedEvent timedEvent) {
    io.telekom.orchest.adapter.mongo.model.TimedEvent doc = timedEventMapper.toDocument(timedEvent);
    cancelScheduled(doc.getId(), doc.getType());
    mongoTimedEventRepository.delete(doc);
  }

  /**
   * Deletes the timer start event for a given process definition and cancels its scheduled task.
   *
   * @param definitionId the process definition ID whose start timer should be removed
   */
  @Override
  public void deleteStartEventByDefinitionId(String definitionId) {
    cancelScheduledFor(
        mongoTimedEventRepository.listByProcessDefinitionIdAndIsStartEvent(definitionId, true));
    mongoTimedEventRepository.deleteTimedEventsByProcessDefinitionIdAndIsStartEvent(
        definitionId, true);
  }

  /**
   * Deletes all pending retry timers for a process instance and cancels their scheduled tasks.
   *
   * @param processInstanceId the process instance ID whose retry timers should be removed
   */
  @Override
  public void deletePendingRetryTimersByProcessInstanceId(String processInstanceId) {
    List<io.telekom.orchest.adapter.mongo.model.TimedEvent> retryTimers =
        mongoTimedEventRepository.findByProcessInstanceIdAndType(
            processInstanceId, io.telekom.orchest.adapter.mongo.model.TimedEvent.Type.TASK_RETRY);
    cancelScheduledFor(retryTimers);
    mongoTimedEventRepository.deleteByProcessInstanceIdAndType(
        processInstanceId, io.telekom.orchest.adapter.mongo.model.TimedEvent.Type.TASK_RETRY);
  }

  private void cancelScheduledFor(List<io.telekom.orchest.adapter.mongo.model.TimedEvent> timers) {
    if (timers == null || timers.isEmpty()) {
      return;
    }
    for (io.telekom.orchest.adapter.mongo.model.TimedEvent timer : timers) {
      cancelScheduled(timer.getId(), timer.getType());
    }
  }

  private void cancelScheduled(
      String timedEventId, io.telekom.orchest.adapter.mongo.model.TimedEvent.Type type) {
    if (timedEventId == null) {
      return;
    }
    taskScheduler.cancelByBusinessKey(taskTypeFor(type), timedEventId);
  }

  private static String taskTypeFor(io.telekom.orchest.adapter.mongo.model.TimedEvent.Type type) {
    return type == io.telekom.orchest.adapter.mongo.model.TimedEvent.Type.TASK_RETRY
        ? TASK_RETRY_TASK_TYPE
        : TIMER_FIRE_TASK_TYPE;
  }
}
