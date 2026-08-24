package io.telekom.orchest.api.core.adapters.data.dto;

import io.telekom.orchest.api.core.request.Variables;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Request to resume a waiting activity within a process instance, optionally supplying variables.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ResumeActivityEventRequest {

  /** The process instance containing the activity to resume. */
  private String processInstanceId;

  /** The BPMN activity element ID to resume. */
  private String activityId;

  /** Variables to merge into the process scope upon resumption. */
  private Variables variables;
}
