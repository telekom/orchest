package io.telekom.orchest.utils;

import io.telekom.orchest.api.ActivatedJob;
import io.telekom.orchest.api.core.adapters.data.dto.WorkerEventRequest;
import io.telekom.orchest.api.core.model.bpmn.node.ServiceTaskNode;
import io.telekom.orchest.api.impl.ActivatedJobValue;
import java.util.HashMap;
import java.util.Map;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

/** Utility class for worker-related operations. */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class WorkerHelper {

  /**
   * Builds an ActivatedJob from a WorkerEventRequest.
   *
   * @param workerEventRequest The worker event request
   * @return The activated job
   */
  public static ActivatedJob buildActivatedJob(WorkerEventRequest workerEventRequest) {
    return ActivatedJobValue.builder()
        .nodeInformation((ServiceTaskNode) workerEventRequest.getNodeInformation())
        .processInstanceId(workerEventRequest.getProcessInstanceId())
        .processDefinitionId(workerEventRequest.getProcessDefinitionId())
        .processDefinitionVersion(workerEventRequest.getVersion())
        .variables(workerEventRequest.getVariables().getVariables())
        .retries(workerEventRequest.getRetriesLeft())
        .build();
  }

  /**
   * Creates a log context map from an activated job.
   *
   * @param activatedJob The activated job
   * @param logVariables Whether to include variables
   * @return Log context map
   */
  public static Map<String, String> getLogContextMap(
      ActivatedJob activatedJob, boolean logVariables) {
    Map<String, String> logContextMap = new HashMap<>();
    logContextMap.put("worker", activatedJob.getType());
    logContextMap.put("processId", activatedJob.getBpmnProcessId());
    logContextMap.put("processInstanceKey", activatedJob.getProcessInstanceKey());
    if (logVariables) {
      logContextMap.put("variables", activatedJob.getVariables());
    }
    return logContextMap;
  }

  /**
   * Extracts specific variables for logging based on configuration.
   *
   * @param activatedJob The activated job
   * @param logVariables Map of variable keys to log keys
   * @return Map of log entries
   */
  public static Map<String, String> getLogVariablesMap(
      ActivatedJob activatedJob, Map<String, String> logVariables) {
    Map<String, String> logMap = new HashMap<>();
    if (logVariables == null || logVariables.isEmpty()) {
      return logMap;
    }

    Map<String, Object> jobVariables = activatedJob.getVariablesMap();
    logVariables.forEach(
        (varKey, logKey) -> {
          Object value = jobVariables.get(varKey);
          if (value != null) {
            logMap.put(logKey, value.toString());
          }
        });

    return logMap;
  }
}
