package io.telekom.orchest.adapter.mongo.model;

import io.telekom.orchest.api.core.adapters.data.dto.WorkerEventRequest;
import io.telekom.orchest.api.core.model.bpmn.IntermediateEventState;
import io.telekom.orchest.api.core.model.bpmn.node.BaseNode;
import io.telekom.orchest.api.core.request.StateChange;
import java.time.LocalDateTime;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

/**
 * MongoDB document representing a scheduled timer or retry event. Covers BPMN timer events (start
 * and intermediate) as well as task retry scheduling.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document
public class TimedEvent {

  /** Unique database identifier. */
  @Id private String id;

  /** External registry ID linking this event to the scheduler. */
  private String timedEventRegistryId;

  /** Current state of the timed event subscription. */
  private IntermediateEventState state;

  /** The type of timed event (timer, start event timer, or task retry). */
  private Type type;

  /** The timer expression value (ISO duration, cron, or date). */
  private String value;

  /** The process definition this event belongs to. */
  private String processDefinitionId;

  /** The version of the process definition. */
  private Integer version;

  /** The process instance this event is associated with (if applicable). */
  private String processInstanceId;

  /** Information about the BPMN node that owns this timer. */
  private BaseNode nodeInformation;

  /** The original worker event request, used for task retry events. */
  private WorkerEventRequest eventRequest; // For task retry event

  /** History of state transitions for this event. */
  private List<StateChange> stateChanges;

  /** Flag indicating if this is a start event timer. */
  @Builder.Default private Boolean isStartEvent = false;

  /** Flag indicating if this event is part of an Event-Based Gateway. */
  @Builder.Default private boolean isLinkedEvent = false;

  /** The ID of the linked Event-Based Gateway document, if applicable. */
  private String linkedEventId; // documentId of the linked EventBasedGateway

  /** The timestamp at which this event should fire. */
  private LocalDateTime triggerAt;

  /** Enum representing the category of timed event. */
  public enum Type {
    TIMER,
    START_EVENT_TIMER,
    TASK_RETRY;
  }
}
