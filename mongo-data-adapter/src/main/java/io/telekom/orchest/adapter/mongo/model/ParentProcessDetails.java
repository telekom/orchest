package io.telekom.orchest.adapter.mongo.model;

import io.telekom.orchest.api.core.model.bpmn.node.BaseNode;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Represents details about a parent process, typically used for linking sub-processes or call
 * activities.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ParentProcessDetails {
  /** The unique identifier of the parent process instance. */
  private String processInstanceId;

  /** Information about the node in the parent process that triggered the current process. */
  private BaseNode nodeInformation;
}
