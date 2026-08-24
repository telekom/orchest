package io.telekom.orchest.api.core.model.bpmn;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Represents a data mapping for input or output in a BPMN node. Maps a variable name to its
 * value/expression.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class DataMapping {
  /** Variable name or data object name */
  private String name;

  /** Value or expression (e.g., variable reference, FEEL expression) */
  private String value;
}
