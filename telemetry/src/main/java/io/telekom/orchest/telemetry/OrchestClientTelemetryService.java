package io.telekom.orchest.telemetry;

import static io.telekom.orchest.telemetry.OrchestTelemetryService.ORCHEST_PREFIX;

import io.micrometer.core.instrument.Tag;
import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * Service for reporting client-side metrics. Tracks client process invocations, worker events, and
 * message/signal events.
 */
@Component
@RequiredArgsConstructor
public class OrchestClientTelemetryService {

  private final OrchestTelemetryService orchestTelemetryService;

  private static final String CLIENT_DEPLOYMENT_METRIC_NAME =
      ORCHEST_PREFIX + "client.bpmn.deployment";
  private static final String CLIENT_PROCESS_INVOCATION_TOTAL_METRIC_NAME =
      ORCHEST_PREFIX + "client.process.invocations";

  private static final String WORKER_METRIC_NAME = ORCHEST_PREFIX + "client.worker.event";
  private static final String MESSAGE_EVENT_METRIC_NAME = ORCHEST_PREFIX + "client.message.event";
  private static final String SIGNAL_EVENT_METRIC_NAME = ORCHEST_PREFIX + "client.signal.event";

  /** Increments the counter for client-initiated process invocations. */
  public void incrementClientProcessInvocationCounter() {
    orchestTelemetryService.incrementMetric(CLIENT_PROCESS_INVOCATION_TOTAL_METRIC_NAME);
  }

  /**
   * Increments the counter for worker events.
   *
   * @param state The state of the worker event (e.g., STARTED, COMPLETED).
   * @param workerId The ID of the worker.
   */
  public void incrementWorkerCounter(String state, String workerId) {
    List<Tag> tags = new ArrayList<>();
    tags.add(Tag.of("state", state));
    tags.add(Tag.of("workerId", workerId));
    orchestTelemetryService.incrementMetric(WORKER_METRIC_NAME, tags);
  }

  /**
   * Increments the counter for message events sent by the client.
   *
   * @param messageName The name of the message.
   */
  public void incrementMessageEventCounter(String messageName) {
    List<Tag> tags = new ArrayList<>();
    tags.add(Tag.of("messageName", messageName));
    orchestTelemetryService.incrementMetric(MESSAGE_EVENT_METRIC_NAME, tags);
  }

  /**
   * Increments the counter for signal events broadcast by the client.
   *
   * @param signalName The name of the signal.
   */
  public void incrementSignalEventCounter(String signalName) {
    List<Tag> tags = new ArrayList<>();
    tags.add(Tag.of("signalName", signalName));
    orchestTelemetryService.incrementMetric(SIGNAL_EVENT_METRIC_NAME, tags);
  }
}
