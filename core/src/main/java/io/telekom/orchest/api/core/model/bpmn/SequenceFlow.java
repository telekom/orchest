package io.telekom.orchest.api.core.model.bpmn;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Represents a sequence flow (edge) between two nodes in a BPMN process. This is stored separately
 * to avoid circular references during serialization.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class SequenceFlow {
  private String id; // SequenceFlow ID
  private String sourceId; // Source node ID
  private String targetId; // Target node ID
}
