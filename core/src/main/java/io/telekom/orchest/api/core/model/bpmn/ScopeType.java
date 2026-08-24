package io.telekom.orchest.api.core.model.bpmn;

/** Identifies the scope level a BPMN node belongs to within the process hierarchy. */
public enum ScopeType {
  /** Node belongs to the root process scope. */
  PROCESS,

  /** Node belongs to a subprocess scope. */
  SUBPROCESS,

  /** Node belongs to an event subprocess scope. */
  EVENT_SUBPROCESS
}
