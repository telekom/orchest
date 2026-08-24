package io.telekom.orchest.timertask;

import io.telekom.orchest.adapter.mongo.MongoTimedEventRepositoryAdapter;
import io.telekom.orchest.api.core.adapters.data.model.TimedEvent;
import io.telekom.orchest.api.core.adapters.data.repository.TimedEventRepository;
import io.telekom.orchest.enginecore.bpmn.OrchestWorkflowEngine;
import io.telekom.orchest.scheduling.api.ScheduledTask;
import io.telekom.orchest.scheduling.api.TaskHandler;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * Handler for {@link MongoTimedEventRepositoryAdapter#TASK_RETRY_TASK_TYPE} — drives BPMN service
 * task progressive-retry events when the scheduler determines they're due.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ProgressiveRetryTaskHandler implements TaskHandler {

  private final TimedEventRepository timedEventRepository;
  private final OrchestWorkflowEngine workflowEngine;

  /** {@inheritDoc} */
  @Override
  public String type() {
    return MongoTimedEventRepositoryAdapter.TASK_RETRY_TASK_TYPE;
  }

  /**
   * Looks up the referenced timed event and triggers a progressive retry via the workflow engine.
   *
   * @param task the scheduled task whose payload is the timed event ID
   */
  @Override
  public void handle(ScheduledTask task) {
    String timedEventId = task.getPayload();
    if (timedEventId == null) {
      log.warn(
          "Progressive retry task id={} has empty payload — nothing to dispatch", task.getId());
      return;
    }
    Optional<TimedEvent> timedEvent = timedEventRepository.findById(timedEventId);
    if (timedEvent.isEmpty()) {
      log.info(
          "Progressive retry task id={} references missing TimedEvent id={} — likely cancelled, skipping",
          task.getId(),
          timedEventId);
      return;
    }
    workflowEngine.handleProgressiveRetry(timedEvent.get());
  }
}
