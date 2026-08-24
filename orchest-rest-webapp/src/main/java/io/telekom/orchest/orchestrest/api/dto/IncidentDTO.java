package io.telekom.orchest.orchestrest.api.dto;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/** Data transfer object representing a process instance incident in API responses. */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class IncidentDTO {

  /** The process instance that encountered the incident. */
  private String processInstanceId;

  /** The process definition the instance belongs to. */
  private String processDefinitionId;

  /** Human-readable description of the incident cause. */
  private String incidentMessage;

  /** The source process instance ID if the incident originated in a sub-process. */
  private String incidentSourceInstanceId;

  /** BPMN element IDs that were active when the incident occurred. */
  private List<String> activeElements;

  /** Current state of the process instance (e.g. INCIDENT). */
  private String state;

  /** Version of the process definition. */
  private Integer version;

  /** Timestamp when the process instance was created (ISO 8601). */
  private String createdAt;

  /** Timestamp when the process instance completed or was cancelled (ISO 8601). */
  private String completedAt;
}
