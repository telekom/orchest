package io.telekom.orchest.timertask;

import io.telekom.orchest.adapter.mongo.MongoTimedEventRepositoryAdapter;
import io.telekom.orchest.api.core.adapters.data.model.TimedEvent;
import io.telekom.orchest.api.core.adapters.data.repository.TimedEventRepository;
import io.telekom.orchest.enginecore.bpmn.OrchestWorkflowEngine;
import io.telekom.orchest.scheduling.api.ScheduledTask;
import io.telekom.orchest.scheduling.api.TaskHandler;
import io.telekom.orchest.telemetry.OrchestEngineTelemetryService;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * Handler for {@link MongoTimedEventRepositoryAdapter#TIMER_FIRE_TASK_TYPE} — fires intermediate
 * catch and start timer events when the distributed scheduler determines they're due.
 *
 * <p>Replaces the previous TTL-index + Mongo change-stream pipeline. Invocation is at-least-once,
 * so this handler relies on the engine's existing idempotency guards (see {@code
 * OrchestWorkflowEngine#handleTimerEvent}).
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class TimerFireTaskHandler implements TaskHandler {

  private final TimedEventRepository timedEventRepository;
  private final OrchestEngineTelemetryService telemetryService;
  private final OrchestWorkflowEngine workflowEngine;

  /** {@inheritDoc} */
  @Override
  public String type() {
    return MongoTimedEventRepositoryAdapter.TIMER_FIRE_TASK_TYPE;
  }

  /**
   * Looks up the referenced timed event and fires it through the workflow engine.
   *
   * @param task the scheduled task whose payload is the timed event ID
   */
  @Override
  public void handle(ScheduledTask task) {
    // Payload carries the BPMN TimedEvent's persistent id — see MongoTimedEventRepositoryAdapter.
    String timedEventId = task.getPayload();
    if (timedEventId == null) {
      log.warn("Timer fire task id={} has empty payload — nothing to dispatch", task.getId());
      return;
    }
    Optional<TimedEvent> timedEvent = timedEventRepository.findById(timedEventId);
    if (timedEvent.isEmpty()) {
      // Cancellation race: the BPMN row was deleted (e.g. linked event won) but the scheduler
      // task fired anyway. Cancellations are best-effort, so this is the expected path.
      log.info(
          "Timer fire task id={} references missing TimedEvent id={} — likely cancelled, skipping",
          task.getId(),
          timedEventId);
      return;
    }
    TimedEvent event = timedEvent.get();
    log.info(
        "Firing timer event id={} type={} processInstanceId={} processDefinitionId={}",
        event.getId(),
        event.getType(),
        event.getProcessInstanceId(),
        event.getProcessDefinitionId());
    telemetryService.incrementTimerEventCounter();
    workflowEngine.handleTimerEvent(event);
  }
}
