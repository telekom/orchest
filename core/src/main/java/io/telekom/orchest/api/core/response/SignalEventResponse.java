package io.telekom.orchest.api.core.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/** Response payload returned after broadcasting a signal event. */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SignalEventResponse {

  /** The unique identifier of the broadcast signal event. */
  private String id;

  /** The name of the signal that was broadcast. */
  private String signalName;
}
