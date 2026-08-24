package io.telekom.orchest.api.core.model.bpmn.node;

import io.telekom.orchest.api.core.model.bpmn.NodeType;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Represents a Service Task node in a BPMN process. A Service Task is an automated activity that
 * invokes an external service or worker.
 */
@Setter
@NoArgsConstructor
public class ServiceTaskNode extends ActivityNode {
  /** The worker type identifier used to route this task to the correct worker. */
  private String workerType;

  /** Maximum number of retries before raising an incident. */
  private Integer retries;

  /**
   * Constructs a new ServiceTaskNode with the specified ID and name.
   *
   * @param id the unique identifier for the node
   * @param name the human-readable name of the node
   */
  public ServiceTaskNode(String id, String name) {
    super(id, name, NodeType.SERVICE_TASK);
  }

  /**
   * Returns the worker type, falling back to properties map for backward compatibility.
   *
   * @return the worker type identifier
   */
  public String getWorkerType() {
    if (workerType != null) return workerType;
    Object val = getProperties().get("taskType");
    return val != null ? val.toString() : null;
  }

  /**
   * Returns the retry count, defaulting to 3 if not configured.
   *
   * @return the maximum number of retries
   */
  public int getRetries() {
    if (retries != null) return retries;
    Object val = getProperties().get("retries");
    return val != null ? Integer.parseInt(val.toString()) : 3;
  }
}
