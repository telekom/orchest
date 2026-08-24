package io.telekom.orchest.api.core.adapters.data.dto;

import io.telekom.orchest.api.core.model.bpmn.NodeState;
import io.telekom.orchest.api.core.model.bpmn.node.BaseNode;
import io.telekom.orchest.api.core.request.StateChange;
import io.telekom.orchest.api.core.request.Variables;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Event dispatched to a worker when an activity requires external execution (service task, send
 * task, etc.). Contains all context the worker needs to perform work and report back.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WorkerEventRequest {

  /** Unique identifier of this worker event. */
  private String eventId;

  /** The process instance that owns the activity. */
  private String processInstanceId;

  /** The BPMN process definition containing the activity. */
  private String processDefinitionId;

  /** The BPMN activity element ID to be executed by the worker. */
  private String activityId;

  /** Human-readable name of the activity. */
  private String activityName;

  /** Worker task type used for routing (e.g. job type). */
  private String type;

  private String errorCode; // Error code for ERROR type events

  /** Whether this event targets the common (shared) worker rather than a dedicated one. */
  private boolean isCommonWorker;

  /** Parent process instance ID when this activity belongs to a call-activity subprocess. */
  private String parentProcessInstanceId;

  /** Version of the process definition. */
  private Integer version;

  /** Current state of the BPMN node. */
  private NodeState state;

  /** Process variables available to the worker. */
  private Variables variables;

  /** Structural information about the BPMN node. */
  private BaseNode nodeInformation;

  /** Ordered list of state changes applied during execution. */
  @Builder.Default private List<StateChange> stateChanges = new ArrayList<>();

  /** Incident message if the worker reports a failure. */
  private String incidentMessage;

  /** Boundary message event details, if applicable. */
  private BoundaryMessageEvent messageEvent;

  /** Error event details when the worker throws a BPMN error. */
  private ErrorEvent errorEvent;

  /** Remaining retry attempts before an incident is raised. */
  @Builder.Default private int retriesLeft = 3;

  /** Total configured retries for this job. */
  @Builder.Default private int retries = 3;

  /** Back-off duration between retry attempts. */
  private Duration retryBackOff;

  /** Describes a BPMN error thrown by a worker. */
  @Data
  @Builder
  @NoArgsConstructor
  @AllArgsConstructor
  public static class ErrorEvent {
    /** Symbolic name of the BPMN error. */
    private String errorName;

    /** Error code matching the BPMN error definition. */
    private String errorCode;

    /** Human-readable message describing the error. */
    private String incidentMessage;
  }

  /** Holds correlation data for a boundary message event attached to the activity. */
  @Data
  @Builder
  @NoArgsConstructor
  @AllArgsConstructor
  public static class BoundaryMessageEvent {
    /** The message name to correlate against. */
    private String messageName;

    /** Correlation key value for message matching. */
    private String correlation;
  }
}
