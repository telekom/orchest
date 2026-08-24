package io.telekom.orchest.orchestrest.api.dto;

import java.util.List;
import java.util.Map;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Data Transfer Object for decision instance information. Represents the execution state and result
 * of a DMN decision table.
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class DecisionInstanceDTO {

  /** The ID of the decision definition. */
  private String decisionId;

  /** The unique ID of the decision instance. */
  private String decisionInstanceId;

  /** The ID of the process instance that triggered this decision. */
  private String processInstanceId;

  /** Input variables used for the decision evaluation. */
  private Map<String, Object> inputVariables;

  /** Output result of the decision evaluation. */
  private Map<String, Object> outputVariables;

  /** The raw XML of the decision definition. */
  private String resourceUTF8XML;

  /** The IDs of the rule that matched (if applicable). */
  private List<String> matchedRuleIds;

  /** The version of the decision definition used. */
  private Integer version;

  /** The state of the decision execution (e.g., EVALUATED, FAILED). */
  private String state;

  /** The ID of the executed rule. */
  private String executedRuleId;

  /** Timestamp when the decision was executed. */
  private String executedAt;
}
