package io.telekom.orchest.api.core.adapters.data.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/** Payload published when an incident (unresolvable error) occurs during process execution. */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class IncidentEventPayload {

  /** The BPMN process definition where the incident originated. */
  private String processDefinitionId;

  /** Version of the process definition. */
  private Integer version;

  /** The process instance that encountered the incident. */
  private String processInstanceId;

  /** Correlation identifier used to trace related events. */
  private String correlationId;

  /** Human-readable message describing the incident cause. */
  private String incidentMessage;

  /** Namespace (tenant) in which the process is running. */
  private String namespace;

  /** The BPMN activity element ID where the incident occurred. */
  private String activityId;

  /** Human-readable name of the activity where the incident occurred. */
  private String activityName;
}
