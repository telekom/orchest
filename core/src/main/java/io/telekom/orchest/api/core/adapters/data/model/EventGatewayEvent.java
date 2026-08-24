package io.telekom.orchest.api.core.adapters.data.model;

import io.telekom.orchest.api.core.model.bpmn.IntermediateEventState;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Persistent record of an intermediate event attached to an event-based gateway within a process
 * instance.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EventGatewayEvent {

  /** Unique identifier for this event record. */
  private String id;

  /** Name of the event (e.g. message name or timer identifier). */
  private String eventName;

  /** BPMN element ID of the intermediate event in the process definition. */
  private String eventId;

  /** Current state of this intermediate event (e.g. waiting, triggered). */
  private IntermediateEventState state;

  /** The process instance this event belongs to. */
  private String processInstanceId;

  /** The sequence flow ID that was taken when this event was triggered. */
  private String executedSequenceId;
}
