package io.telekom.orchest.api.core.model.bpmn;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/** Configuration for BPMN multi-instance (loop) execution on an activity. */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MultiInstanceLoopCharacteristics {
  /** Whether instances execute sequentially (true) or in parallel (false). */
  private boolean isSequential;

  /** FEEL expression resolving to the collection to iterate over. */
  private String collection; // Expression: =items

  /** Variable name bound to the current iteration element. */
  private String elementVariable; // Variable name: item

  /** Optional FEEL expression; when true, remaining instances are skipped. */
  private String completionCondition; // Expression

  /** Variable name for the aggregated output collection. */
  private String outputCollection;

  /** Expression or variable name for each iteration's output element. */
  private String outputElement;
}
