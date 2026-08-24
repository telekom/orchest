package io.telekom.orchest.api.core.model.dmn;

import java.util.ArrayList;
import java.util.List;
import lombok.Data;
import lombok.NoArgsConstructor;

/** Represents a single rule (row) in a DMN decision table mapping inputs to outputs. */
@Data
@NoArgsConstructor
public class Rule {
  /** Unique identifier for this rule. */
  private String id;

  /** FEEL expressions defining the input conditions for this rule. */
  private List<String> inputEntries = new ArrayList<>(); // FEEL expressions for input conditions

  /** FEEL expressions defining the output values when this rule matches. */
  private List<String> outputEntries = new ArrayList<>(); // FEEL expressions for output values

  /** Human-readable description of this rule. */
  private String description;

  /** Position of this rule in the decision table (1-based). */
  private int ruleNumber; // Order in the decision table

  /**
   * Adds a FEEL expression as an input condition entry.
   *
   * @param inputEntry the FEEL expression for an input condition
   */
  public void addInputEntry(String inputEntry) {
    this.inputEntries.add(inputEntry);
  }

  /**
   * Adds a FEEL expression as an output value entry.
   *
   * @param outputEntry the FEEL expression for an output value
   */
  public void addOutputEntry(String outputEntry) {
    this.outputEntries.add(outputEntry);
  }
}
