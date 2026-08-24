package io.telekom.orchest.client.clients.processstate;

import io.telekom.orchest.adapter.kafka.KafkaUtils;
import io.telekom.orchest.client.OrchestProperties;
import io.telekom.orchest.client.listener.DynamicKafkaConsumer;
import io.telekom.orchest.client.model.ProcessState;
import io.telekom.orchest.rest.client.OrchesTRestClient;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;

/**
 * Periodically polls the OrchesT ProcessState API and pauses/resumes dynamic Kafka consumers based
 * on each process's enabled/disabled state.
 *
 * <p>Only considers processIds configured in {@code orchest.processIds}. If a processId has no
 * entry in the API, it is treated as enabled.
 */
@Slf4j
@RequiredArgsConstructor
public class ProcessStateScheduler {

  private final OrchesTRestClient orchesTRestClient;
  private final DynamicKafkaConsumer dynamicKafkaConsumer;
  private final OrchestProperties orchestProperties;

  private final Map<String, Boolean> lastKnownState = new ConcurrentHashMap<>();

  /** Periodically polls the process-state API and pauses/resumes consumers as needed. */
  @Scheduled(fixedDelayString = "${orchest.processState.pollIntervalMs:10000}")
  public void syncProcessStates() {
    try {
      Map<String, ProcessState> stateMap = fetchProcessStateMap();
      Set<String> configuredProcessIds = Set.copyOf(orchestProperties.getProcessIds());

      for (String processId : configuredProcessIds) {
        boolean shouldBeEnabled = isProcessEnabled(stateMap, processId);
        Boolean previousState = lastKnownState.get(processId);

        if (previousState != null && previousState == shouldBeEnabled) {
          continue;
        }

        String topic = KafkaUtils.getClientWorkerEventTopic(processId);
        if (shouldBeEnabled) {
          dynamicKafkaConsumer.resumeConsumer(topic, orchestProperties.getWorkerThreadCount());
          log.info("Resumed consumer for processId={}", processId);
        } else {
          dynamicKafkaConsumer.pauseConsumer(topic);
          log.info("Paused consumer for processId={}", processId);
        }
        lastKnownState.put(processId, shouldBeEnabled);
      }
    } catch (Exception e) {
      log.warn("Failed to sync process states: {}", e.getMessage());
    }
  }

  /**
   * Fetches all process states from the REST API, keyed by process ID.
   *
   * @return map of process ID to state, or empty map on failure
   */
  public Map<String, ProcessState> fetchProcessStateMap() {
    try {
      List<ProcessState> states = orchesTRestClient.getProcessStateApi().getProcessStates();
      if (states == null || states.isEmpty()) {
        return Map.of();
      }
      return states.stream()
          .filter(s -> s.getProcessId() != null)
          .collect(Collectors.toMap(ProcessState::getProcessId, Function.identity(), (a, b) -> b));
    } catch (Exception e) {
      log.warn("Failed to fetch process state on startup: {}", e.getMessage());
      return Map.of();
    }
  }

  private boolean isProcessEnabled(Map<String, ProcessState> stateMap, String processId) {
    ProcessState state = stateMap.get(processId);
    // no entry = enabled; entry with status=true = enabled; status=false = disabled
    return state == null || !Boolean.FALSE.equals(state.getStatus());
  }
}
