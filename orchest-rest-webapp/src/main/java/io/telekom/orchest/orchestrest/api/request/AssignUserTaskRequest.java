package io.telekom.orchest.orchestrest.api.request;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/** Request body for reassigning a user task to a different user. */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AssignUserTaskRequest {

  /** The user ID of the new assignee. */
  @NotBlank(message = "Assignee is required")
  private String assignee;
}
