package io.telekom.orchest.api.core.request;

import java.util.Map;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/** Request payload for compensating (rolling back) a single process instance. */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CompensateInstanceRequest {

  /** The process instance to compensate. */
  private String processInstanceId;

  /** The process definition ID this instance belongs to. */
  private String processDefinitionId;

  /** The process definition version. */
  private Integer version;

  /** Variables to pass to the compensation flow. */
  private Map<String, Object> variables;
}
