package io.telekom.orchest.api.core.model.bpmn;

/** Enumerates all supported BPMN element types within a process definition. */
public enum NodeType {
  START_EVENT,
  END_EVENT,

  // Tasks
  TASK,
  USER_TASK,
  SERVICE_TASK,
  RECEIVE_TASK,
  SEND_TASK,
  BUSINESS_RULE_TASK,
  SCRIPT_TASK,
  MANUAL_TASK,

  // Subprocesses
  SUB_PROCESS,
  CALL_ACTIVITY,

  // Gateways
  EXCLUSIVE_GATEWAY,
  PARALLEL_GATEWAY,
  INCLUSIVE_GATEWAY,
  EVENT_BASED_GATEWAY,

  // Events
  BOUNDARY_EVENT,
  INTERMEDIATE_CATCH_EVENT,
  INTERMEDIATE_THROW_EVENT
}
