package io.telekom.orchest.api.core.adapters.data.model;

import io.telekom.orchest.api.core.model.bpmn.node.BaseNode;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/** Tracks the call-activity link between a child subprocess and its parent process instance. */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ParentProcessActivity {

  /** The process instance ID of the parent process. */
  private String processInstanceId;

  /** The BPMN node in the parent process that spawned the child (e.g., a Call Activity). */
  private BaseNode linkedNode;
}
