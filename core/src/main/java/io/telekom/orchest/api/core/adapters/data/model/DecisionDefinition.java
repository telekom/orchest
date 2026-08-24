package io.telekom.orchest.api.core.adapters.data.model;

import io.telekom.orchest.api.core.model.dmn.Decision;
import java.time.OffsetDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/** Persistent model for a deployed DMN decision definition containing one or more decisions. */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class DecisionDefinition {
  /** Unique identifier for this decision definition record. */
  private String id;

  /** Logical definition ID used to group versions of the same decision definition. */
  private String definitionId;

  /** IDs of individual decisions contained in this definition. */
  private List<String> decisionIds;

  /** Version number of this decision definition. */
  private Integer version;

  /** Human-readable name of the decision definition. */
  private String name;

  /** Namespace used to scope this decision definition. */
  private String namespace;

  /** Raw DMN XML content of the definition. */
  private String definitionXML;

  /** Map of decision ID to parsed Decision objects. */
  private Map<String, Decision> decisions = new HashMap<>();

  /** Timestamp when this definition was created. */
  private OffsetDateTime createdAt;

  /**
   * Adds a decision to this definition's decision map.
   *
   * @param decision the decision to add, keyed by its ID
   */
  public void addDecision(Decision decision) {
    decisions.put(decision.getId(), decision);
  }

  /**
   * Retrieves a decision by its ID.
   *
   * @param decisionId the ID of the decision to look up
   * @return an Optional containing the decision if found, or empty otherwise
   */
  public Optional<Decision> getDecision(String decisionId) {
    return Optional.ofNullable(decisions.get(decisionId));
  }
}
