package io.telekom.orchest.api.core.model.dmn;

import java.util.ArrayList;
import java.util.List;
import lombok.Data;
import lombok.RequiredArgsConstructor;

/** Represents a DMN decision table with inputs, outputs, rules, and a hit policy. */
@Data
@RequiredArgsConstructor
public class DecisionTable {
  /** Unique identifier for this decision table. */
  private String id;

  /** Hit policy determining how matching rules are aggregated. */
  private String
      hitPolicy; // UNIQUE, FIRST, PRIORITY, ANY, COLLECT, ORDER, RULE_ORDER, OUTPUT_ORDER

  /** Aggregation function used with COLLECT hit policy (SUM, COUNT, MIN, MAX). */
  private String aggregation; // SUM, COUNT, MIN, MAX (for COLLECT hit policy)

  /** Input columns defining the conditions to evaluate. */
  private List<Input> inputs = new ArrayList<>();

  /** Output columns defining the result values. */
  private List<Output> outputs = new ArrayList<>();

  /** Rules mapping input conditions to output values. */
  private List<Rule> rules = new ArrayList<>();

  /**
   * Adds an input column to this decision table.
   *
   * @param input the input definition to add
   */
  public void addInput(Input input) {
    this.inputs.add(input);
  }

  /**
   * Adds an output column to this decision table.
   *
   * @param output the output definition to add
   */
  public void addOutput(Output output) {
    this.outputs.add(output);
  }

  /**
   * Adds a rule to this decision table.
   *
   * @param rule the rule to add
   */
  public void addRule(Rule rule) {
    this.rules.add(rule);
  }
}
