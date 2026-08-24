package io.telekom.orchest.orchestrest.api.dto;

import io.telekom.orchest.api.core.model.bpmn.ExecutionLogEntry;
import java.util.List;
import java.util.Map;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/** Data transfer object representing a process instance in API responses. */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ProcessInstanceDTO {

  /** The process definition this instance belongs to. */
  private String processDefinitionId;

  /** Unique identifier of this process instance. */
  private String processInstanceId;

  /** Parent process instance ID if this is a sub-process invocation. */
  private String parentProcessInstanceId;

  /** Process instance variables (may be filtered for sensitive data). */
  private Map<String, Object> variables;

  /** BPMN XML with stroke annotations showing execution path. */
  private String bpmnXML;

  /** Version of the process definition used. */
  private Integer version;

  /** Incident message if the instance is in INCIDENT state. */
  private String incidentMessage;

  /** Currently active BPMN element IDs. */
  private List<String> activeElements;

  /** Current state of the instance (ACTIVE, COMPLETED, INCIDENT, CANCELLED, TERMINATED). */
  private String state;

  /** Execution history keyed by timestamp. */
  private Map<String, ExecutionLogEntry> sequenceExecutions;

  /** Timestamp when the instance was created (ISO 8601). */
  private String createdAt;

  /** Timestamp when the instance completed (ISO 8601). */
  private String completedAt;

  /** Timestamp of the last activity execution (ISO 8601). */
  private String lastActivityAt;
}
