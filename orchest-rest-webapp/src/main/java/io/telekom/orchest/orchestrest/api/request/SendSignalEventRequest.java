package io.telekom.orchest.orchestrest.api.request;

import jakarta.validation.constraints.NotBlank;
import java.util.Map;
import lombok.Data;
import lombok.NoArgsConstructor;

/** Request payload for broadcasting a BPMN signal event to all subscribed process instances. */
@Data
@NoArgsConstructor
public class SendSignalEventRequest {

  /** The BPMN signal name to broadcast. */
  @NotBlank private String signalName;

  /** Variables to pass along with the signal event. */
  private Map<String, Object> variables;
}
