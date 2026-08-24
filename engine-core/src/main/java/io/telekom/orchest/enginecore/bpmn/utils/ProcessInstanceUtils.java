package io.telekom.orchest.enginecore.bpmn.utils;

import io.telekom.orchest.api.core.adapters.data.model.ProcessInstance;
import io.telekom.orchest.api.core.model.bpmn.NodeType;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Utility class for operations related to {@link ProcessInstance}. Contains helper methods for
 * checking wait states and incidents.
 */
@Slf4j
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class ProcessInstanceUtils {

  /**
   * Determines if a given node type represents a wait state. A wait state is a point in the process
   * where execution pauses and waits for an external trigger (e.g., user action, message, timer, or
   * service task completion).
   *
   * @param type The node type to check.
   * @return true if the node type is a wait state, false otherwise.
   */
  public static boolean isWaitState(NodeType type) {
    return type == NodeType.USER_TASK
        || type == NodeType.RECEIVE_TASK
        || type == NodeType.EVENT_BASED_GATEWAY
        || type == NodeType.SERVICE_TASK
        || // Service Tasks wait for Kafka completion events
        type == NodeType.CALL_ACTIVITY
        || type == NodeType.SUB_PROCESS;
  }

  /**
   * Checks if a process instance has an active incident. If an incident is present, it logs a
   * warning.
   *
   * @param processInstance The process instance to check.
   * @return true if the instance has an incident, false otherwise.
   */
  public static boolean checkIfInstanceHasIncident(ProcessInstance processInstance) {
    if (processInstance.isHasIncident()) {
      log.warn(
          "Process instance {} has an incident. Stopping execution of outgoing nodes. Incident: {}",
          processInstance.getProcessInstanceId(),
          processInstance.getIncidentMessage());
      return true; // Stop execution immediately
    }
    return false;
  }
}
