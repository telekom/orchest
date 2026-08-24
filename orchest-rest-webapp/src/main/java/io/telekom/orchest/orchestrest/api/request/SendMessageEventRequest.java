package io.telekom.orchest.orchestrest.api.request;

import jakarta.validation.constraints.NotBlank;
import java.util.Map;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Request payload for publishing a BPMN message event to correlate with waiting process instances.
 */
@Data
@NoArgsConstructor
public class SendMessageEventRequest {

  /** The BPMN message name to publish. */
  @NotBlank private String messageName;

  /** Correlation key used to match this message to a specific process instance. */
  private String correlationKey;

  /** Variables to pass along with the message event. */
  private Map<String, Object> variables;
}
