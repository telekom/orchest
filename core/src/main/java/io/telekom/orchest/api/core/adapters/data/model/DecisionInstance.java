package io.telekom.orchest.api.core.adapters.data.model;

import io.telekom.orchest.api.core.model.dmn.DIState;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/** Persistent record of a single DMN decision evaluation within a process instance. */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DecisionInstance {

  /** Unique identifier for this decision instance. */
  private String decisionInstanceId;

  /** The decision definition ID that was evaluated. */
  private String definitionId;

  /** The process instance that triggered this evaluation. */
  private String processInstanceId;

  /** Version of the decision definition used. */
  private int version;

  /** Raw DMN XML resource as a UTF-8 string. */
  private String resourceXMLUTF8String;

  /** Input variables passed to the decision evaluation. */
  private Map<String, Object> inputVariables;

  /** Output variables produced by the decision evaluation. */
  private Map<String, Object> outputVariables;

  /** Encrypted representation of input variables. */
  private String encInputVariables;

  /** Encrypted representation of output variables. */
  private String encOutputVariables;

  /** IDs of rules that matched during evaluation. */
  private List<String> matchedRuleIds;

  /** Current state of this decision instance (e.g. evaluated, failed). */
  private DIState state;

  /** Timestamp when this decision was executed. */
  private OffsetDateTime executedAt;
}
