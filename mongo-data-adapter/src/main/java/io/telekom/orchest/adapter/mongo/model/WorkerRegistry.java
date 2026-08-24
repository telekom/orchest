package io.telekom.orchest.adapter.mongo.model;

import io.telekom.orchest.api.core.request.WorkerRegistryRequest;
import java.time.OffsetDateTime;
import java.util.Set;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.mongodb.core.mapping.Document;

/**
 * MongoDB document representing a registered set of workers for a process definition. Tracks which
 * worker types are available to handle tasks in a given namespace.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document
public class WorkerRegistry {

  /** Unique database identifier. */
  @Id private String id;

  /** The namespace the workers are registered under. */
  private String nameSpace;

  /** The process definition these workers serve. */
  private String processDefinitionId;

  /** Timestamp of the most recent registration or heartbeat. */
  @LastModifiedDate private OffsetDateTime lastRegisteredAt;

  /** Set of worker descriptors registered for this process. */
  private Set<WorkerRegistryRequest.WorkerInfo> workerInfos;
}
