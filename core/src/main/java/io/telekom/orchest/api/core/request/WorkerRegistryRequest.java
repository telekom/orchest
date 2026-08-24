package io.telekom.orchest.api.core.request;

import java.util.Set;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/** Request payload for registering available workers with the engine. */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WorkerRegistryRequest {

  /** The namespace (application identifier) these workers belong to. */
  private String namespace;

  /** Set of worker definitions to register. */
  private Set<WorkerInfo> workers;

  /** Describes a single worker type available for task execution. */
  @Data
  @NoArgsConstructor
  @AllArgsConstructor
  public static class WorkerInfo {
    /** The worker type identifier matching service task definitions. */
    private String type;

    /** Whether this worker is shared across multiple process definitions. */
    private boolean isCommonWorker;
  }
}
