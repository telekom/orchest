package io.telekom.orchest.api.core.request;

import io.telekom.orchest.api.core.model.bpmn.node.BaseNode;
import java.util.Map;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Request object used to initiate a new process instance. Encapsulates all necessary information to
 * start a workflow.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProcessInvocationRequest {
  /** The identifier of the process definition to instantiate. */
  private String processDefinitionId;

  /** The unique identifier for the process instance. If not provided, one will be generated. */
  private String processInstanceId;

  /**
   * The specific version of the process definition to use. If null, the latest version is typically
   * used.
   */
  private Integer version;

  /** Initial variables to pass to the process instance. */
  private Map<String, Object> variables;

  // Parent process details
  /** The identifier of the parent process instance, if this is a sub-process. */
  private String parentProcessInstanceId;

  /** Information about the node in the parent process that triggered this instance. */
  private BaseNode nodeInformation;
}
