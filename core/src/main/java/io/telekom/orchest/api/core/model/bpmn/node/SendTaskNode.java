package io.telekom.orchest.api.core.model.bpmn.node;

import io.telekom.orchest.api.core.model.bpmn.NodeType;
import lombok.NoArgsConstructor;

/** Represents a BPMN send task that dispatches a message to an external participant. */
@NoArgsConstructor
public class SendTaskNode extends ActivityNode {
  /** The message name to send. */
  private String messageRef;

  /**
   * Constructs a new SendTaskNode with the specified ID and name.
   *
   * @param id the unique identifier for the node
   * @param name the human-readable name of the node
   */
  public SendTaskNode(String id, String name) {
    super(id, name, NodeType.SEND_TASK);
  }

  /**
   * Returns the message reference, falling back to properties map for backward compatibility.
   *
   * @return the message name, or null if not set
   */
  public String getMessageRef() {
    if (messageRef != null) return messageRef;
    Object val = getProperties().get("messageRef");
    return val != null ? val.toString() : null;
  }

  public void setMessageRef(String messageRef) {
    this.messageRef = messageRef;
  }
}
