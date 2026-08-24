package io.telekom.orchest.alerting.api;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/** Request body identifying the user or system performing an alert lifecycle action. */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Request body identifying the actor performing an action")
public class ActorRequest {

  @Schema(
      description = "Identity of the user or system performing the action",
      example = "john.doe@example.com")
  private String actor;
}
