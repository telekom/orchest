package io.telekom.orchest.adapter.mongo.model;

import io.telekom.orchest.api.core.model.bpmn.IntermediateEventState;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

/**
 * MongoDB document representing an Event-Based Gateway event subscription. Used to track which
 * events (message, signal, timer) a process instance is waiting for at a gateway.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document
public class EventGatewayEvent {

  /** Unique identifier for the event subscription. */
  @Id private String id;

  /** The name of the event (e.g., message name, signal name). */
  private String eventName;

  /** The ID of the event node in the BPMN process. */
  private String eventId;

  /** The state of the event subscription. */
  private IntermediateEventState state;

  /** The ID of the process instance waiting for this event. */
  private String processInstanceId;

  /** The ID of the sequence flow executed when this event occurs. */
  private String executedSequenceId;
}
