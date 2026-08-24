package io.telekom.orchest.api.core.adapters.data.model;

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

/** Persistent model representing a scheduled timer event or task retry in the workflow engine. */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TimedEvent {

  /** Unique document identifier. */
  private String id;

  /** Registry ID linking this event to its timer registration record. */
  private String timedEventRegistryId;

  /** Current state of this timed event (e.g., WAITING, TRIGGERED). */
  private IntermediateEventState state;

  /** The kind of timed event (TIMER, START_EVENT_TIMER, or TASK_RETRY). */
  private Type type;

  /** Timer expression value (ISO 8601 duration, date, or cycle). */
  private String value;

  /** The process definition this event belongs to. */
  private String processDefinitionId;

  /** Version of the process definition. */
  private Integer version;

  /** The process instance this event is associated with (null for start event timers). */
  private String processInstanceId;

  /** BPMN node information associated with this timed event. */
  private BaseNode nodeInformation;

  /** Worker event request payload for task retry events. */
  private WorkerEventRequest eventRequest; // For task retry event

  /** History of state transitions for this event. */
  private List<StateChange> stateChanges;

  /** Whether this timer is a start event (triggers new process instances on schedule). */
  @Builder.Default private Boolean isStartEvent = false;

  /** Whether this event is linked to an EventBasedGateway. */
  @Builder.Default private Boolean isLinkedEvent = false;

  /** Document ID of the linked EventBasedGateway, if any. */
  private String linkedEventId; // documentId of the linked EventBasedGateway

  /** The exact date/time when this event should fire. */
  private LocalDateTime triggerAt;

  /** Classification of timed events. */
  public enum Type {
    /** Standard intermediate or boundary timer event. */
    TIMER,
    /** Timer attached to a start event (creates instances on schedule). */
    START_EVENT_TIMER,
    /** Scheduled retry of a failed worker task. */
    TASK_RETRY;
  }
}
