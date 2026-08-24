package io.telekom.orchest.adapter.mongo.model;

import io.telekom.orchest.api.core.model.bpmn.IntermediateEventState;
import io.telekom.orchest.api.core.model.bpmn.node.BaseNode;
import java.time.OffsetDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

/**
 * MongoDB document for storing Message Event subscriptions. Tracks message catch events (start or
 * intermediate) that process instances are waiting for.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document
public class MessageEventStore {

  /** Unique identifier. */
  @Id private String id;

  /** The correlation key used to match the message to a specific process instance. */
  private String correlationKey;

  /** The name of the message. */
  private String messageName;

  /** The state of the message subscription. */
  private IntermediateEventState state;

  /** The ID of the process instance (if applicable). */
  private String processInstanceId;

  /** The ID of the process definition. */
  private String processDefinitionId;

  /** Information about the BPMN node waiting for the message. */
  private BaseNode nodeInformation;

  /** Flag indicating if this is a message start event. */
  @Builder.Default private Boolean isStartEvent = false;

  /** Flag indicating if this event is part of an Event-Based Gateway. */
  private boolean isLinkedEvent;

  /** The ID of the linked Event-Based Gateway document, if applicable. */
  private String linkedEventId; // documentId of the linked EventBasedGateway

  /** Timestamp when the subscription was created. */
  @CreatedDate private OffsetDateTime createdAt;
}
