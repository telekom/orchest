package io.telekom.orchest.api.core.adapters.data.model;

import io.telekom.orchest.api.core.model.bpmn.IntermediateEventState;
import io.telekom.orchest.api.core.model.bpmn.node.BaseNode;
import java.time.OffsetDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/** Persistent model representing a BPMN message event subscription waiting for correlation. */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MessageEventStore {

  /** Unique document identifier. */
  private String id;

  /** Correlation key used to match incoming messages to this subscription. */
  private String correlationKey;

  /** The BPMN message name this subscription listens for. */
  private String messageName;

  /** Current state of this message event (e.g., WAITING, TRIGGERED). */
  private IntermediateEventState state;

  /** The process instance that owns this subscription. */
  private String processInstanceId;

  /** The process definition this subscription belongs to. */
  private String processDefinitionId;

  /** BPMN node information associated with this message event. */
  private BaseNode nodeInformation;

  /** Whether this message event is a start event (triggers new process instances). */
  @Builder.Default private Boolean isStartEvent = false;

  /** Whether this event is linked to an EventBasedGateway. */
  private boolean isLinkedEvent;

  /** Document ID of the linked EventBasedGateway, if any. */
  private String linkedEventId; // documentId of the linked EventBasedGateway

  /** Timestamp when this subscription was created. */
  private OffsetDateTime createdAt;
}
