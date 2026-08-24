package io.telekom.orchest.api.core.model.dmn;

import java.util.HashMap;
import java.util.Map;
import lombok.Data;
import lombok.RequiredArgsConstructor;

/** Represents a single DMN decision element containing a decision table and metadata. */
@Data
@RequiredArgsConstructor
public class Decision {
  /** Unique identifier for this decision. */
  private final String id;

  /** Human-readable name of the decision. */
  private String name;

  /** The decision table that defines the evaluation logic. */
  private DecisionTable decisionTable;

  /** The business question this decision answers. */
  private String question;

  /** Description of allowed answer values. */
  private String allowedAnswers;

  /** Additional properties associated with this decision. */
  private final Map<String, Object> properties = new HashMap<>();
}
