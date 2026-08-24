package io.telekom.orchest.api.core.model.bpmn.node;

import io.telekom.orchest.api.core.model.bpmn.NodeType;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Represents a Call Activity node in a BPMN process. Call Activities invoke another process
 * definition as a subprocess.
 */
@Getter
@Setter
@NoArgsConstructor
public class CallActivityNode extends ActivityNode {
  /** The BPMN process ID of the child process to invoke. */
  private String calledProcessId;

  /** Whether to propagate all parent process variables to the child (default true). */
  private Boolean propagateAllParentVariables;

  /** Whether to propagate all child process variables back to the parent (default true). */
  private Boolean propagateAllChildVariables;

  /**
   * Constructs a new CallActivityNode with the specified ID and name.
   *
   * @param id the unique identifier for the node
   * @param name the human-readable name of the node
   */
  public CallActivityNode(String id, String name) {
    super(id, name, NodeType.CALL_ACTIVITY);
  }

  /**
   * Returns whether all parent variables are propagated to the child process.
   *
   * @return true if all parent variables should be propagated (defaults to true)
   */
  public boolean isPropagateAllParentVariables() {
    if (propagateAllParentVariables != null) return propagateAllParentVariables;
    Object val = getProperties().get("propagateAllParentVariables");
    return val instanceof Boolean b ? b : true;
  }

  /**
   * Returns whether all child variables are propagated back to the parent process.
   *
   * @return true if all child variables should be propagated (defaults to true)
   */
  public boolean isPropagateAllChildVariables() {
    if (propagateAllChildVariables != null) return propagateAllChildVariables;
    Object val = getProperties().get("propagateAllChildVariables");
    return val instanceof Boolean b ? b : true;
  }
}
