package io.telekom.orchest.api.core.adapters.data.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Event published by the engine when a connector service task is reached. The connector service
 * consumes this event, executes the connector implementation and resumes the workflow for the
 * referenced activity.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ConnectorTaskRequest {

  /** Unique identifier of this connector task event. */
  private String eventId;

  /** The process instance that triggered the connector task. */
  private String processInstanceId;

  /** The BPMN process definition containing the connector task. */
  private String processDefinitionId;

  /** The BPMN activity element ID of the connector service task. */
  private String activityId;

  /** Human-readable name of the connector service task. */
  private String activityName;

  /** Camunda connector task-definition type, e.g. {@code io.orchest:http-json:1}. */
  private String connectorType;

  /** Version of the process definition. */
  private Integer version;
}
