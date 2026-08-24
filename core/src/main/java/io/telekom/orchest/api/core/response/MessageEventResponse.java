package io.telekom.orchest.api.core.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/** Response payload returned after publishing a message event. */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MessageEventResponse {

  /** The unique identifier of the published message event. */
  private String id;

  /** The name of the message that was published. */
  private String messageName;

  /** The correlation key used to route the message to the target process instance. */
  private String correlationKey;
}
