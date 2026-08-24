package io.telekom.orchest.api.core.adapters.data.model;

import io.telekom.orchest.api.core.request.WorkerRegistryRequest;
import java.time.OffsetDateTime;
import java.util.Set;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/** Persistent model storing the set of workers registered for a given process definition. */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WorkerRegistry {

  /** Unique document identifier. */
  private String id;

  /** Namespace/tenant this registry entry belongs to. */
  private String nameSpace;

  /** The process definition these workers are registered against. */
  private String processDefinitionId;

  /** Timestamp of the most recent worker heartbeat or registration. */
  private OffsetDateTime lastRegisteredAt;

  /** Set of individual worker descriptors registered under this entry. */
  private Set<WorkerRegistryRequest.WorkerInfo> workerInfos;
}
