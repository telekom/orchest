package io.telekom.orchest.api.core.request;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/** Request payload for publishing a message event to correlate with waiting process instances. */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MessageEventRequest {
  /** Unique identifier for this message event request. */
  private String id;

  /** The message name to correlate against. */
  private String messageName;

  /** The correlation key used to match with a specific process instance. */
  private String correlationKey;

  /** Variables to merge into the process instance upon correlation. */
  private Variables variables;

  /** State changes to record in the execution log. */
  private List<StateChange> stateChanges;
}
