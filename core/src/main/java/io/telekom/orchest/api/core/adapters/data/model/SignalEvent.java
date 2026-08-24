package io.telekom.orchest.api.core.adapters.data.model;

import io.telekom.orchest.api.core.model.bpmn.IntermediateEventState;
import io.telekom.orchest.api.core.model.bpmn.node.BaseNode;
import java.time.OffsetDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/** Persistent model representing a BPMN signal event subscription waiting to be triggered. */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SignalEvent {

  /** Unique document identifier. */
  private String id;

  /** The signal reference ID from the BPMN definition. */
  private String signalId;

  /** Human-readable signal name. */
  private String signalName;

  /** Current state of this signal event (e.g., WAITING, TRIGGERED). */
  private IntermediateEventState state;

  /** The process instance that owns this subscription. */
  private String processInstanceId;

  /** The process definition this subscription belongs to (used for signal start events). */
  private String processDefinitionId; // for signal start event

  /** BPMN node information associated with this signal event. */
  private BaseNode nodeInformation;

  /** Whether this signal event is a start event (triggers new process instances). */
  @Builder.Default private Boolean isStartEvent = false;

  /** Whether this event is linked to an EventBasedGateway. */
  private boolean isLinkedEvent;

  /** Document ID of the linked EventBasedGateway, if any. */
  private String linkedEventId; // documentId of the linked EventBasedGateway

  /** Timestamp when this subscription was created. */
  private OffsetDateTime createdAt;
}
