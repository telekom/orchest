package io.telekom.orchest.adapter.mongo.model;

import io.telekom.orchest.api.core.adapters.data.model.ParentProcessActivity;
import io.telekom.orchest.api.core.model.bpmn.ExecutionLogEntry;
import io.telekom.orchest.api.core.model.bpmn.PIState;
import java.time.OffsetDateTime;
import java.util.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.domain.Persistable;
import org.springframework.data.mongodb.core.mapping.Document;

/**
 * MongoDB document representing a BPMN Process Instance. Stores the complete state of a running or
 * completed process, including variables, execution history, and active nodes.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document
public class ProcessInstance implements Persistable<String> {
  /** Unique database identifier. */
  @Id private String id;

  /** The unique identifier of the process instance. */
  private String processInstanceId;

  /** The ID of the process definition this instance belongs to. */
  private String processDefinitionId;

  /** The version of the process definition. */
  private Integer version;

  /** Current variables of the process instance. */
  private Map<String, Object> variables = new HashMap<>();

  /** Encrypted version of the instanceVariables. */
  private String encVariables;

  /** Set of currently active node IDs (tokens). */
  private Set<String> activeNodeIds = new HashSet<>();

  /** Execution history log, keyed by node ID/execution ID. */
  private Map<String, ExecutionLogEntry> executionHistory = new LinkedHashMap<>();

  /**
   * Generic execution state/metadata map for tracking various execution-related data. Examples:
   * parallel gateway tokens, inclusive gateway tokens, loop counters, etc.
   */
  private Map<String, Object> executionState = new HashMap<>();

  /** Flag indicating if the instance has an active incident (error). */
  private boolean hasIncident = false;

  /** Flag indicating if the instance was executed in dynamic mode or not. */
  private boolean dynamicFlow;

  /** Message describing the incident, if any. */
  private String incidentMessage;

  /**
   * The process instance ID of the child that originally caused this incident. Non-null indicates
   * this is a propagated incident from a child process — not retryable directly.
   */
  private String incidentSourceInstanceId;

  /** Flag indicating if the instance has completed execution. */
  private boolean completed = false;

  /** Details about the parent process if this is a sub-process instance. */
  private ParentProcessActivity parentProcesActivity;

  /** Current state of the process instance (e.g., RUNNING, COMPLETED, FAILED). */
  private PIState state;

  /** Correlation ID for linking with external systems or messages. */
  private List<String> correlationIds; // can be orderId, publicIdentifier or correlationId

  /** Timestamp when the instance was created. */
  @CreatedDate private OffsetDateTime createdAt;

  /** Timestamp when the instance completed. */
  private OffsetDateTime completedAt;

  /** Timestamp when the instance was last modified. */
  @LastModifiedDate private OffsetDateTime lastModifiedAt;

  @Override
  public boolean isNew() {
    return id == null;
  }
}
