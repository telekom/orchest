package io.telekom.orchest.api.core.request;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/** Request payload for batch compensation of multiple process instances. */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BatchCompensateInstanceRequest {

  /** List of process instance IDs to compensate. */
  private List<String> processInstanceIds;

  /** The process definition ID these instances belong to. */
  private String processDefinitionId;

  /** The process definition version. */
  private Integer version;
}
