package io.telekom.orchest.api.core.request;

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
public class DynamicProcessInvocationRequest {

  /** The BPMN 2.0 XML content as a UTF-8 string defining the process to execute. */
  private String utf8BpmnXML;

  /** The unique identifier for the process instance. If not provided, one will be generated. */
  private String processInstanceId;

  /** Initial variables to pass to the process instance. */
  private Map<String, Object> variables;
}
