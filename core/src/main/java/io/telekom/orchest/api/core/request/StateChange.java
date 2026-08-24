package io.telekom.orchest.api.core.request;

import io.telekom.orchest.api.core.model.bpmn.NodeState;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/** Records a state transition for a BPMN node with a UTC timestamp. */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class StateChange {
  /** The node state after this transition. */
  private NodeState state;

  /** UTC timestamp when this state change occurred. */
  private OffsetDateTime timestamp = OffsetDateTime.now(ZoneOffset.UTC);

  /**
   * Constructs a state change with the current UTC timestamp.
   *
   * @param state the new node state
   */
  public StateChange(NodeState state) {
    this.state = state;
  }
}
