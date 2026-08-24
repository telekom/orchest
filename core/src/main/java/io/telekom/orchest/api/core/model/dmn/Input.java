package io.telekom.orchest.api.core.model.dmn;

import lombok.Data;
import lombok.NoArgsConstructor;

/** Represents an input column in a DMN decision table. */
@Data
@NoArgsConstructor
public class Input {
  /** Unique identifier for this input. */
  private String id;

  /** Display label for this input column. */
  private String label;

  /** Name of the input variable. */
  private String name;

  /** FEEL expression used to extract the input value. */
  private String inputExpression; // FEEL expression

  /** Variable name bound to this input. */
  private String inputVariable; // Variable name

  /** Data type reference for validation. */
  private String typeRef; // Data type reference
}
