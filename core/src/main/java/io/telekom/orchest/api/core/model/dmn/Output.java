package io.telekom.orchest.api.core.model.dmn;

import lombok.Data;
import lombok.NoArgsConstructor;

/** Represents an output column in a DMN decision table. */
@Data
@NoArgsConstructor
public class Output {
  /** Unique identifier for this output. */
  private String id;

  /** Display label for this output column. */
  private String label;

  /** Name of the output variable. */
  private String name;

  /** Data type reference for the output value. */
  private String typeRef; // Data type reference
}
