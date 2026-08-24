package io.telekom.orchest.api.core.request;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/** Request payload for broadcasting a signal event to all subscribed process instances. */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SignalEventRequest {
  /** Unique identifier for this signal event request. */
  private String signalId;

  /** The signal name to broadcast. */
  private String signalName;

  /** Variables to pass to processes that catch this signal. */
  private Variables variables;

  /** State changes to record in the execution log. */
  private List<StateChange> stateChanges;
}
