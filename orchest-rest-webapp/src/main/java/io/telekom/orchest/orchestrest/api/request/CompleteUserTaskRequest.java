package io.telekom.orchest.orchestrest.api.request;

import java.util.Map;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Request body for completing a user task. Allows the user to submit output variables along with
 * the task completion.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CompleteUserTaskRequest {

  /**
   * Output variables to merge into the process instance upon task completion. These are the results
   * of the user's work on the task.
   */
  private Map<String, Object> variables;
}
