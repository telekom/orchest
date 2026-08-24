package io.telekom.orchest.api.core.model.bpmn.node;

import io.telekom.orchest.api.core.model.bpmn.NodeType;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/** Represents a BPMN script task that executes an inline script during process execution. */
@Getter
@Setter
@NoArgsConstructor
public class ScriptTaskNode extends ActivityNode {
  /** The language/format of the script (e.g., "groovy", "javascript"). */
  private String scriptFormat;

  /** The inline script source code to execute. */
  private String script;

  /** The variable name where the script result will be stored. */
  private String resultVariable;

  /**
   * Constructs a new ScriptTaskNode with the specified ID and name.
   *
   * @param id the unique identifier for the node
   * @param name the human-readable name of the node
   */
  public ScriptTaskNode(String id, String name) {
    super(id, name, NodeType.SCRIPT_TASK);
  }
}
