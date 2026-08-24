package io.telekom.orchest.api.core.model.bpmn.node;

import io.telekom.orchest.api.core.model.bpmn.NodeType;
import java.util.ArrayList;
import java.util.List;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/** Represents an embedded BPMN subprocess containing its own set of child nodes. */
@Getter
@Setter
@NoArgsConstructor
public class SubProcessNode extends ActivityNode {
  /** IDs of all nodes contained within this subprocess. */
  private List<String> childNodeIds = new ArrayList<>();

  /** ID of the start event node within this subprocess. */
  private String startNodeId;

  /** Whether this subprocess is triggered by an event (event subprocess). */
  private Boolean triggeredByEvent;

  /**
   * Returns whether this subprocess is an event subprocess.
   *
   * @return true if triggered by an event (defaults to false)
   */
  public boolean isTriggeredByEvent() {
    if (triggeredByEvent != null) return triggeredByEvent;
    Object val = getProperties().get("triggeredByEvent");
    return val instanceof Boolean b && b;
  }

  /**
   * Constructs a new SubProcessNode with the specified ID and name.
   *
   * @param id the unique identifier for the node
   * @param name the human-readable name of the node
   */
  public SubProcessNode(String id, String name) {
    super(id, name, NodeType.SUB_PROCESS);
    this.childNodeIds = new ArrayList<>();
  }

  /**
   * Adds a child node ID to this subprocess. This method only stores the ID to avoid circular
   * references.
   *
   * @param childNodeId The ID of the child node
   * @param nodeType The type of the child node (used to identify start events)
   */
  public void addChildNode(String childNodeId, NodeType nodeType) {
    if (childNodeId != null && !childNodeIds.contains(childNodeId)) {
      this.childNodeIds.add(childNodeId);
    }
    if (nodeType == NodeType.START_EVENT && this.startNodeId == null) {
      this.startNodeId = childNodeId;
    }
  }

  /**
   * Adds a child node ID to this subprocess. Convenience method that extracts the node type from
   * the ProcessDefinition.
   *
   * @param childNodeId The ID of the child node
   */
  public void addChildNode(String childNodeId) {
    if (childNodeId != null && !childNodeIds.contains(childNodeId)) {
      this.childNodeIds.add(childNodeId);
    }
  }
}
