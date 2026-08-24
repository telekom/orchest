package io.telekom.orchest.adapter.mongo.model;

import io.telekom.orchest.api.core.model.dmn.DIState;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

/**
 * MongoDB document representing a DMN Decision Instance. Records the execution of a decision,
 * including inputs, outputs, matched rules, and state.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document
public class DecisionInstance {

  /** Unique database identifier. */
  @Id private String id;

  /** The unique ID of this decision instance execution. */
  private String decisionInstanceId;

  /** The ID of the decision definition that was executed. */
  private String definitionId;

  /** The ID of the process instance that invoked this decision. */
  private String processInstanceId;

  /** The version of the decision definition. */
  private int version;

  /** The raw XML content of the decision definition at the time of execution. */
  private String resourceXMLUTF8String;

  /** The input variables passed to the decision. */
  private Map<String, Object> inputVariables;

  /** The output variables resulting from the decision. */
  private Map<String, Object> outputVariables;

  /** List of rule IDs that matched during evaluation. */
  private List<String> matchedRuleIds;

  /** Encrypted representation of the input variables (for sensitive data). */
  private String encInputVariables;

  /** Encrypted representation of the output variables (for sensitive data). */
  private String encOutputVariables;

  /** The state of the decision instance (e.g., EXECUTED, FAILED). */
  private DIState state;

  /** Timestamp when the decision was executed. */
  @CreatedDate private OffsetDateTime executedAt;
}
