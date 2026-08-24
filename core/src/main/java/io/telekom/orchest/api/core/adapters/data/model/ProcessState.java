package io.telekom.orchest.api.core.adapters.data.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/** Persistent model tracking the enabled/disabled status of a deployed process definition. */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProcessState {

  /** Unique document identifier. */
  private String id;

  /** The process definition ID this state applies to. */
  private String processId;

  /** Whether the process is currently active (true) or suspended (false). */
  private boolean status;
}
