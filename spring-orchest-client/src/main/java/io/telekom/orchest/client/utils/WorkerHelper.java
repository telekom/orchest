package io.telekom.orchest.client.utils;

import io.telekom.orchest.api.core.adapters.data.dto.WorkerEventRequest;
import io.telekom.orchest.api.core.model.bpmn.node.ServiceTaskNode;
import io.telekom.orchest.client.annotations.ActivatedJob;
import io.telekom.orchest.client.annotations.value.ActivatedJobValue;
import java.util.HashMap;
import java.util.Map;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

/** Static utility methods for building {@link ActivatedJob} instances and MDC log context maps. */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class WorkerHelper {

  /**
   * Builds an {@link ActivatedJob} from a worker event request.
   *
   * @param workerEventRequest the incoming worker event
   * @return the activated job containing variables and metadata
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
   * Creates an MDC context map with worker metadata for structured logging.
   *
   * @param activatedJob the current job
   * @param logVariables whether to include serialized variables in the context
   * @return the MDC context map
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
   * Extracts configured variable values from the job's variables map for logging.
   *
   * @param activatedJob the current job
   * @param logVariables map of variable keys to log (key=variable name, value=label)
   * @return the extracted variable values as strings
   */
  public static Map<String, String> getLogVariablesMap(
      ActivatedJob activatedJob, Map<String, String> logVariables) {
    Map<String, Object> variablesMap = activatedJob.getVariablesMap();
    Map<String, String> logMap = new HashMap<>();
    logVariables.forEach(
        (key, value) -> {
          if (variablesMap.containsKey(key)) {
            logMap.put(key, variablesMap.get(key).toString());
          }
        });
    return logMap;
  }
}
